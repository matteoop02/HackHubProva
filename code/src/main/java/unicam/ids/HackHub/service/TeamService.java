package unicam.ids.HackHub.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import unicam.ids.HackHub.dto.requests.team.CreateTeamRequest;
import unicam.ids.HackHub.dto.responses.TeamResponse;
import unicam.ids.HackHub.exceptions.BusinessLogicException;
import unicam.ids.HackHub.exceptions.ResourceNotFoundException;
import unicam.ids.HackHub.model.Hackathon;
import unicam.ids.HackHub.model.Team;
import unicam.ids.HackHub.model.User;
import unicam.ids.HackHub.repository.HackathonRepository;
import unicam.ids.HackHub.repository.TeamRepository;
import unicam.ids.HackHub.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final HackathonRepository hackathonRepository;

    public TeamService(TeamRepository teamRepository, UserRepository userRepository,
            HackathonRepository hackathonRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.hackathonRepository = hackathonRepository;
    }

    public TeamResponse createTeam(Authentication authentication, CreateTeamRequest request) {
        User creator = userRepository.findByUsernameAndIsDeletedFalse(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato o eliminato"));

        Hackathon hackathon = hackathonRepository.findById(request.hackathonId())
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon non trovato"));

        Team team = Team.builder()
                .name(request.name())
                .isPublic(request.isPublic())
                .hackathon(hackathon)
                .teamLeader(creator)
                .members(new ArrayList<>(List.of(creator)))
                .build();

        return mapToResponse(teamRepository.save(team));
    }

    public void leaveTeam(Authentication authentication) {
        User user = userRepository.findByUsernameAndIsDeletedFalse(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato o eliminato"));

        Team team = user.getTeam();
        if (team == null) {
            throw new BusinessLogicException("L'utente non fa parte di alcun team");
        }

        if (team.getTeamLeader().getId().equals(user.getId())) {
            throw new BusinessLogicException(
                    "Il leader non può lasciare il team: elimina il team o nomina un altro leader");
        }

        team.removeMember(user);
        user.setTeam(null);
        teamRepository.save(team);
        userRepository.save(user);
    }

    private TeamResponse mapToResponse(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .isPublic(team.isPublic())
                .hackathonId(team.getHackathon() != null ? team.getHackathon().getId() : null)
                .leaderId(team.getTeamLeader() != null ? team.getTeamLeader().getId() : null)
                .memberIds(team.getMembers().stream().map(User::getId).collect(Collectors.toList()))
                .build();
    }
}
