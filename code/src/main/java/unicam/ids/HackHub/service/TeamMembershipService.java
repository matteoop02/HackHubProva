package unicam.ids.HackHub.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unicam.ids.HackHub.enums.TeamRole;
import unicam.ids.HackHub.exceptions.BusinessLogicException;
import unicam.ids.HackHub.exceptions.ResourceNotFoundException;
import unicam.ids.HackHub.model.Team;
import unicam.ids.HackHub.model.TeamMembership;
import unicam.ids.HackHub.model.User;
import unicam.ids.HackHub.repository.TeamMembershipRepository;
import unicam.ids.HackHub.repository.TeamRepository;
import unicam.ids.HackHub.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamMembershipService {

    private final TeamMembershipRepository teamMembershipRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public TeamMembershipService(TeamMembershipRepository teamMembershipRepository,
            TeamRepository teamRepository,
            UserRepository userRepository) {
        this.teamMembershipRepository = teamMembershipRepository;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    public Team getCurrentTeam(User user) {
        return teamMembershipRepository.findByUser(user)
                .map(TeamMembership::getTeam)
                .orElse(null);
    }

    public boolean isLeader(User user, Team team) {
        return teamMembershipRepository.findByUserAndTeam(user, team)
                .map(membership -> membership.getRole() == TeamRole.LEADER)
                .orElse(false);
    }

    public User getLeader(Team team) {
        return teamMembershipRepository.findByTeamAndRole(team, TeamRole.LEADER)
                .map(TeamMembership::getUser)
                .orElse(team.getTeamLeader());
    }

    public List<User> getMembers(Team team) {
        return teamMembershipRepository.findByTeam(team).stream()
                .map(TeamMembership::getUser)
                .collect(Collectors.toList());
    }

    public List<Long> getMemberIds(Team team) {
        return getMembers(team).stream().map(User::getId).collect(Collectors.toList());
    }

    public int countMembers(Team team) {
        return (int) teamMembershipRepository.countByTeam(team);
    }

    public boolean hasTeam(User user) {
        return teamMembershipRepository.findByUser(user).isPresent();
    }

    @Transactional
    public TeamMembership addMembership(User user, Team team, TeamRole role) {
        if (teamMembershipRepository.existsByUserAndTeam(user, team)) {
            throw new BusinessLogicException("L'utente fa gia' parte del team");
        }

        TeamMembership membership = teamMembershipRepository.save(TeamMembership.builder()
                .user(user)
                .team(team)
                .role(role)
                .build());

        syncLegacyState(team);
        return membership;
    }

    @Transactional
    public void removeMembership(User user, Team team) {
        TeamMembership membership = teamMembershipRepository.findByUserAndTeam(user, team)
                .orElseThrow(() -> new ResourceNotFoundException("Appartenenza al team non trovata"));
        teamMembershipRepository.delete(membership);
        syncLegacyState(team);
    }

    @Transactional
    public void syncLegacyState(Team team) {
        Team managedTeam = teamRepository.findById(team.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Team non trovato"));

        List<TeamMembership> memberships = teamMembershipRepository.findByTeam(managedTeam);
        List<User> members = memberships.stream().map(TeamMembership::getUser).collect(Collectors.toList());
        User leader = memberships.stream()
                .filter(membership -> membership.getRole() == TeamRole.LEADER)
                .map(TeamMembership::getUser)
                .findFirst()
                .orElse(null);

        managedTeam.setMembers(members);
        managedTeam.setTeamLeader(leader);
        teamRepository.save(managedTeam);

        List<User> previousLinkedUsers = userRepository.findAll().stream()
                .filter(user -> user.getTeam() != null && user.getTeam().getId().equals(managedTeam.getId()))
                .collect(Collectors.toList());
        previousLinkedUsers.forEach(user -> user.setTeam(null));
        userRepository.saveAll(previousLinkedUsers);

        members.forEach(user -> user.setTeam(managedTeam));
        userRepository.saveAll(members);
    }
}
