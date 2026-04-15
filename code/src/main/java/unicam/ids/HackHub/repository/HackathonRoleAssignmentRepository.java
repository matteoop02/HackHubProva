package unicam.ids.HackHub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unicam.ids.HackHub.enums.HackathonRole;
import unicam.ids.HackHub.model.Hackathon;
import unicam.ids.HackHub.model.HackathonRoleAssignment;
import unicam.ids.HackHub.model.User;

import java.util.List;
import java.util.Optional;

public interface HackathonRoleAssignmentRepository extends JpaRepository<HackathonRoleAssignment, Long> {
    Optional<HackathonRoleAssignment> findByUserAndHackathonAndRole(User user, Hackathon hackathon, HackathonRole role);
    List<HackathonRoleAssignment> findByHackathon(Hackathon hackathon);
    List<HackathonRoleAssignment> findByHackathonAndRole(Hackathon hackathon, HackathonRole role);
    List<HackathonRoleAssignment> findByUser(User user);
    boolean existsByUserAndHackathonAndRole(User user, Hackathon hackathon, HackathonRole role);
}
