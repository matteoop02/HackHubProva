package unicam.ids.HackHub.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unicam.ids.HackHub.enums.HackathonRole;
import unicam.ids.HackHub.exceptions.BusinessLogicException;
import unicam.ids.HackHub.exceptions.ResourceNotFoundException;
import unicam.ids.HackHub.model.Hackathon;
import unicam.ids.HackHub.model.HackathonRoleAssignment;
import unicam.ids.HackHub.model.User;
import unicam.ids.HackHub.repository.HackathonRepository;
import unicam.ids.HackHub.repository.HackathonRoleAssignmentRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class HackathonRoleAssignmentService {

    private final HackathonRoleAssignmentRepository hackathonRoleAssignmentRepository;
    private final HackathonRepository hackathonRepository;

    public HackathonRoleAssignmentService(HackathonRoleAssignmentRepository hackathonRoleAssignmentRepository,
            HackathonRepository hackathonRepository) {
        this.hackathonRoleAssignmentRepository = hackathonRoleAssignmentRepository;
        this.hackathonRepository = hackathonRepository;
    }

    public boolean hasRole(User user, Hackathon hackathon, HackathonRole role) {
        return hackathonRoleAssignmentRepository.existsByUserAndHackathonAndRole(user, hackathon, role)
                || hasLegacyRole(user, hackathon, role);
    }

    public boolean hasAnyAssignment(User user, HackathonRole role) {
        return hackathonRoleAssignmentRepository.findByUser(user).stream()
                .anyMatch(assignment -> assignment.getRole() == role);
    }

    public User getOrganizer(Hackathon hackathon) {
        return hackathonRoleAssignmentRepository.findByHackathonAndRole(hackathon, HackathonRole.ORGANIZER).stream()
                .map(HackathonRoleAssignment::getUser)
                .findFirst()
                .orElse(hackathon.getOrganizer());
    }

    public User getJudge(Hackathon hackathon) {
        return hackathonRoleAssignmentRepository.findByHackathonAndRole(hackathon, HackathonRole.JUDGE).stream()
                .map(HackathonRoleAssignment::getUser)
                .findFirst()
                .orElse(hackathon.getJudge());
    }

    public Set<User> getMentors(Hackathon hackathon) {
        List<User> assignedMentors = hackathonRoleAssignmentRepository.findByHackathonAndRole(hackathon, HackathonRole.MENTOR)
                .stream()
                .map(HackathonRoleAssignment::getUser)
                .toList();

        if (!assignedMentors.isEmpty()) {
            return assignedMentors.stream().collect(Collectors.toSet());
        }

        return hackathon.getMentors();
    }

    @Transactional
    public void assignRole(User user, Hackathon hackathon, HackathonRole role) {
        if (hackathonRoleAssignmentRepository.existsByUserAndHackathonAndRole(user, hackathon, role)) {
            throw new BusinessLogicException("Ruolo hackathon gia' assegnato a questo utente");
        }

        hackathonRoleAssignmentRepository.save(HackathonRoleAssignment.builder()
                .user(user)
                .hackathon(hackathon)
                .role(role)
                .build());

        syncLegacyState(hackathon);
    }

    @Transactional
    public void syncLegacyState(Hackathon hackathon) {
        Hackathon managedHackathon = hackathonRepository.findById(hackathon.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon non trovato"));

        User organizer = hackathonRoleAssignmentRepository.findByHackathonAndRole(managedHackathon, HackathonRole.ORGANIZER)
                .stream()
                .map(HackathonRoleAssignment::getUser)
                .findFirst()
                .orElse(managedHackathon.getOrganizer());

        User judge = hackathonRoleAssignmentRepository.findByHackathonAndRole(managedHackathon, HackathonRole.JUDGE)
                .stream()
                .map(HackathonRoleAssignment::getUser)
                .findFirst()
                .orElse(managedHackathon.getJudge());

        Set<User> mentors = hackathonRoleAssignmentRepository.findByHackathonAndRole(managedHackathon, HackathonRole.MENTOR)
                .stream()
                .map(HackathonRoleAssignment::getUser)
                .collect(Collectors.toSet());

        managedHackathon.setOrganizer(organizer);
        managedHackathon.setJudge(judge);
        if (!mentors.isEmpty()) {
            managedHackathon.setMentors(mentors);
        }

        hackathonRepository.save(managedHackathon);
    }

    private boolean hasLegacyRole(User user, Hackathon hackathon, HackathonRole role) {
        return switch (role) {
            case ORGANIZER -> hackathon.getOrganizer() != null && hackathon.getOrganizer().getId().equals(user.getId());
            case JUDGE -> hackathon.getJudge() != null && hackathon.getJudge().getId().equals(user.getId());
            case MENTOR -> hackathon.getMentors().stream().anyMatch(mentor -> mentor.getId().equals(user.getId()));
        };
    }
}
