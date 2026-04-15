package unicam.ids.HackHub.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import unicam.ids.HackHub.dto.requests.DeclareWinningTeamRequest;
import unicam.ids.HackHub.dto.requests.hackathon.CreateHackathonRequest;
import unicam.ids.HackHub.dto.responses.HackathonResponse;
import unicam.ids.HackHub.enums.HackathonRole;
import unicam.ids.HackHub.enums.HackathonState;
import unicam.ids.HackHub.exceptions.BusinessLogicException;
import unicam.ids.HackHub.exceptions.ResourceNotFoundException;
import unicam.ids.HackHub.exceptions.UnauthorizedAccessException;
import unicam.ids.HackHub.model.Hackathon;
import unicam.ids.HackHub.model.Team;
import unicam.ids.HackHub.model.User;
import unicam.ids.HackHub.repository.HackathonRepository;
import unicam.ids.HackHub.repository.TeamRepository;
import unicam.ids.HackHub.repository.UserRepository;
import unicam.ids.HackHub.util.RoleNames;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HackathonService {

    private final HackathonRepository hackathonRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final EmailService emailService;
    private final HackathonRoleAssignmentService hackathonRoleAssignmentService;

    public HackathonService(HackathonRepository hackathonRepository, UserRepository userRepository,
            TeamRepository teamRepository, EmailService emailService,
            HackathonRoleAssignmentService hackathonRoleAssignmentService) {
        this.hackathonRepository = hackathonRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.emailService = emailService;
        this.hackathonRoleAssignmentService = hackathonRoleAssignmentService;
    }

    public List<HackathonResponse> getHackathons(boolean isAuthenticated) {
        List<Hackathon> hackathons = isAuthenticated ? 
            hackathonRepository.findAll() : 
            hackathonRepository.findAllByIsPublic(true);

        return hackathons.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public HackathonResponse createHackathon(Authentication authentication, CreateHackathonRequest request) {
        Optional<Hackathon> hackathonOptional = hackathonRepository.findByName(request.name());
        if(hackathonOptional.isPresent()) {
            throw new IllegalArgumentException("Hakcathon con il nome scelto già esistente");
        }
        User organizer = userRepository.findByUsernameAndIsDeletedFalse(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato o eliminato"));

        Hackathon hackathon = Hackathon.builder()
                .name(request.name())
                .place(request.place())
                .regulation(request.regulation())
                .subscriptionDeadline(request.subscriptionDeadline())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .reward(request.reward())
                .maxTeamSize(request.maxTeamSize())
                .isPublic(request.isPublic())
                .state(HackathonState.IN_ISCRIZIONE)
                .organizer(organizer)
                .build();

        Hackathon savedHackathon = hackathonRepository.save(hackathon);
        hackathonRoleAssignmentService.assignRole(organizer, savedHackathon, HackathonRole.ORGANIZER);
        return mapToResponse(savedHackathon);
    }

    private HackathonResponse mapToResponse(Hackathon hackathon) {
        return HackathonResponse.builder()
                .id(hackathon.getId())
                .name(hackathon.getName())
                .place(hackathon.getPlace())
                .regulation(hackathon.getRegulation())
                .subscriptionDeadline(hackathon.getSubscriptionDeadline())
                .startDate(hackathon.getStartDate())
                .endDate(hackathon.getEndDate())
                .reward(hackathon.getReward())
                .maxTeamSize(hackathon.getMaxTeamSize())
                .isPublic(hackathon.getIsPublic())
                .state(hackathon.getState())
                .organizerName(hackathonRoleAssignmentService.getOrganizer(hackathon) != null
                        ? hackathonRoleAssignmentService.getOrganizer(hackathon).getName() + " "
                        + hackathonRoleAssignmentService.getOrganizer(hackathon).getSurname()
                        : "N/A")
                .build();
    }

    public void startHackathon(Authentication authentication, Long id) {
        Hackathon hackathon = hackathonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon non trovato"));
        User user = userRepository.findByUsernameAndIsDeletedFalse(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato o eliminato"));
        
        if (!hackathonRoleAssignmentService.hasRole(user, hackathon, HackathonRole.ORGANIZER)) {
            throw new unicam.ids.HackHub.exceptions.UnauthorizedAccessException("Solo l'organizzatore può avviare l'hackathon");
        }
        
        hackathon.start();
        hackathonRepository.save(hackathon);
    }

    public void closeHackathonSubscriptions(Authentication authentication, Long id) {
        Hackathon hackathon = hackathonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon non trovato"));
        User user = userRepository.findByUsernameAndIsDeletedFalse(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato o eliminato"));
        
        if (!hackathonRoleAssignmentService.hasRole(user, hackathon, HackathonRole.ORGANIZER)) {
            throw new unicam.ids.HackHub.exceptions.UnauthorizedAccessException("Solo l'organizzatore può chiudere le iscrizioni");
        }
        
        hackathon.closeSubscriptions();
        hackathonRepository.save(hackathon);
    }
}
