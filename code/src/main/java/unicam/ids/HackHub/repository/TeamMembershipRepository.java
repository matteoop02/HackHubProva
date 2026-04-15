package unicam.ids.HackHub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unicam.ids.HackHub.enums.TeamRole;
import unicam.ids.HackHub.model.Team;
import unicam.ids.HackHub.model.TeamMembership;
import unicam.ids.HackHub.model.User;

import java.util.List;
import java.util.Optional;

public interface TeamMembershipRepository extends JpaRepository<TeamMembership, Long> {
    Optional<TeamMembership> findByUser(User user);
    Optional<TeamMembership> findByUserAndTeam(User user, Team team);
    List<TeamMembership> findByTeam(Team team);
    Optional<TeamMembership> findByTeamAndRole(Team team, TeamRole role);
    boolean existsByUserAndTeam(User user, Team team);
    long countByTeam(Team team);
}
