package unicam.ids.HackHub.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import unicam.ids.HackHub.dto.requests.DeclareWinningTeamRequest;
import unicam.ids.HackHub.dto.requests.hackathon.CreateHackathonRequest;
import unicam.ids.HackHub.dto.responses.HackathonResponse;
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
import java.util.stream.Collectors;

@Service
public class HackathonManagementService {

    private final HackathonRepository hackathonRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final EmailService emailService;

    public HackathonManagementService(HackathonRepository hackathonRepository, UserRepository userRepository,
            TeamRepository teamRepository, EmailService emailService) {
        this.hackathonRepository = hackathonRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.emailService = emailService;
    }

    public List<HackathonResponse> getHackathons(boolean isAuthenticated) {
        List<Hackathon> hackathons = isAuthenticated
                ? hackathonRepository.findAll()
                : hackathonRepository.findAllByIsPublic(true);

        return hackathons.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public HackathonResponse createHackathon(Authentication authentication, CreateHackathonRequest request) {
        if (hackathonRepository.findByName(request.name()).isPresent()) {
            throw new BusinessLogicException("Hackathon con il nome scelto gia' esistente");
        }

        User organizer = userRepository.findByUsernameAndIsDeletedFalse(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato o eliminato"));

        if (!RoleNames.ORGANIZER.equals(organizer.getRole().getName())) {
            throw new UnauthorizedAccessException("Solo un organizzatore puo' creare un hackathon");
        }

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

        return mapToResponse(hackathonRepository.save(hackathon));
    }

    public void startHackathon(Authentication authentication, Long id) {
        Hackathon hackathon = getManagedHackathon(authentication, id);
        hackathon.start();
        hackathonRepository.save(hackathon);
    }

    public void closeHackathonSubscriptions(Authentication authentication, Long id) {
        Hackathon hackathon = getManagedHackathon(authentication, id);
        hackathon.closeSubscriptions();
        hackathonRepository.save(hackathon);
    }

    public HackathonResponse declareWinningTeam(Authentication authentication, Long hackathonId,
            DeclareWinningTeamRequest request) {
        Hackathon hackathon = getManagedHackathon(authentication, hackathonId);

        Team winner = teamRepository.findById(request.teamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team non trovato"));

        if (winner.getHackathon() == null || !winner.getHackathon().getId().equals(hackathon.getId())) {
            throw new BusinessLogicException("Il team selezionato non partecipa a questo hackathon");
        }

        hackathon.declareWinner(winner);
        Hackathon savedHackathon = hackathonRepository.save(hackathon);

        winner.getMembers().forEach(member -> emailService.sendEmail(
                member.getEmail(),
                "Il tuo team ha vinto l'hackathon " + hackathon.getName(),
                "Complimenti, il team '" + winner.getName() + "' e' stato proclamato vincitore dall'organizzatore."
        ));

        return mapToResponse(savedHackathon);
    }

    private Hackathon getManagedHackathon(Authentication authentication, Long id) {
        Hackathon hackathon = hackathonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon non trovato"));

        if (!hackathon.getOrganizer().getUsername().equals(authentication.getName())) {
            throw new UnauthorizedAccessException("Solo l'organizzatore dell'hackathon puo' eseguire questa operazione");
        }

        return hackathon;
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
                .isPublic(Boolean.TRUE.equals(hackathon.getIsPublic()))
                .state(hackathon.getState())
                .organizerName(hackathon.getOrganizer() != null
                        ? hackathon.getOrganizer().getName() + " " + hackathon.getOrganizer().getSurname()
                        : "N/A")
                .winningTeamId(hackathon.getTeamWinner() != null ? hackathon.getTeamWinner().getId() : null)
                .winningTeamName(hackathon.getTeamWinner() != null ? hackathon.getTeamWinner().getName() : null)
                .build();
    }
}
