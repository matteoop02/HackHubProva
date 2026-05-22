package unicam.ids.HackHub.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import unicam.ids.HackHub.dto.requests.AssignJudgeRequest;
import unicam.ids.HackHub.dto.requests.AssignMentorsRequest;
import unicam.ids.HackHub.dto.requests.DeclareWinningTeamRequest;
import unicam.ids.HackHub.dto.requests.hackathon.CreateHackathonRequest;
import unicam.ids.HackHub.dto.responses.PrizePaymentStatusResponse;
import unicam.ids.HackHub.enums.HackathonRole;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HackathonManagementService {

    private final HackathonRepository hackathonRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final EmailService emailService;
    private final PaymentService paymentService;
    private final TeamMembershipService teamMembershipService;
    private final HackathonRoleAssignmentService hackathonRoleAssignmentService;

    public HackathonManagementService(HackathonRepository hackathonRepository, UserRepository userRepository,
            TeamRepository teamRepository, EmailService emailService, PaymentService paymentService,
            TeamMembershipService teamMembershipService,
            HackathonRoleAssignmentService hackathonRoleAssignmentService) {
        this.hackathonRepository = hackathonRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.emailService = emailService;
        this.paymentService = paymentService;
        this.teamMembershipService = teamMembershipService;
        this.hackathonRoleAssignmentService = hackathonRoleAssignmentService;
    }

    public List<HackathonResponse> getHackathons(boolean isAuthenticated) {
        List<Hackathon> hackathons = isAuthenticated
                ? hackathonRepository.findAll()
                : hackathonRepository.findAllByIsPublic(true);

        return hackathons.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public HackathonResponse getHackathonById(Long hackathonId, boolean isAuthenticated) {
        Hackathon hackathon = hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon non trovato"));

        if (!isAuthenticated && !Boolean.TRUE.equals(hackathon.getIsPublic())) {
            throw new UnauthorizedAccessException("Questo hackathon non e' visibile senza autenticazione");
        }

        return mapToResponse(hackathon);
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

        Hackathon savedHackathon = hackathonRepository.save(hackathon);
        hackathonRoleAssignmentService.assignRole(organizer, savedHackathon, HackathonRole.ORGANIZER);
        return mapToResponse(savedHackathon);
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

        if (hackathon.getState() != HackathonState.IN_VALUTAZIONE) {
            throw new BusinessLogicException("Il team vincitore puo' essere proclamato solo quando l'hackathon e' in valutazione");
        }

        Team winner = teamRepository.findById(request.teamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team non trovato"));

        if (winner.getHackathon() == null || !winner.getHackathon().getId().equals(hackathon.getId())) {
            throw new BusinessLogicException("Il team selezionato non partecipa a questo hackathon");
        }

        hackathon.declareWinner(winner);
        Hackathon savedHackathon = hackathonRepository.save(hackathon);

        teamMembershipService.getMembers(winner).forEach(member -> emailService.sendEmail(
                member.getEmail(),
                "Il tuo team ha vinto l'hackathon " + hackathon.getName(),
                "Complimenti, il team '" + winner.getName() + "' e' stato proclamato vincitore dall'organizzatore."
        ));

        return mapToResponse(savedHackathon);
    }

    public void payPrize(Authentication authentication, Long hackathonId) {
        Hackathon hackathon = getManagedHackathon(authentication, hackathonId);
        paymentService.payWinningTeam(hackathon);
    }

    public PrizePaymentStatusResponse getPaymentStatus(Authentication authentication, Long hackathonId) {
        Hackathon hackathon = getManagedHackathon(authentication, hackathonId);
        return paymentService.getPaymentStatus(hackathon);
    }

    public void assignJudge(Authentication authentication, Long hackathonId, AssignJudgeRequest request) {
        Hackathon hackathon = getManagedHackathon(authentication, hackathonId);
        User judge = getActiveUser(request.judgeId(), "Giudice non trovato");

        if (!RoleNames.JUDGE.equals(judge.getRole().getName())) {
            throw new BusinessLogicException("L'utente selezionato non ha ruolo base GIUDICE");
        }

        hackathonRoleAssignmentService.assignRole(judge, hackathon, HackathonRole.JUDGE);
    }

    public void assignMentors(Authentication authentication, Long hackathonId, AssignMentorsRequest request) {
        Hackathon hackathon = getManagedHackathon(authentication, hackathonId);

        for (Long mentorId : request.mentorIds()) {
            User mentor = getActiveUser(mentorId, "Mentore non trovato");
            if (!RoleNames.MENTOR.equals(mentor.getRole().getName())) {
                throw new BusinessLogicException("L'utente con id " + mentorId + " non ha ruolo base MENTOR");
            }
            hackathonRoleAssignmentService.assignRole(mentor, hackathon, HackathonRole.MENTOR);
        }
    }

    public void removeMentor(Authentication authentication, Long hackathonId, Long mentorId) {
        Hackathon hackathon = getManagedHackathon(authentication, hackathonId);
        User mentor = getActiveUser(mentorId, "Mentore non trovato");
        hackathonRoleAssignmentService.removeRole(mentor, hackathon, HackathonRole.MENTOR);
    }

    @Scheduled(cron = "0 * * * * *")
    public void updateHackathonStatesByDate() {
        LocalDateTime now = LocalDateTime.now();
        startHackathonsReadyToRun(now);
        moveExpiredHackathonsToEvaluation(now);
    }

    private Hackathon getManagedHackathon(Authentication authentication, Long id) {
        Hackathon hackathon = hackathonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon non trovato"));

        User user = userRepository.findByUsernameAndIsDeletedFalse(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));

        if (!hackathonRoleAssignmentService.hasRole(user, hackathon, HackathonRole.ORGANIZER)) {
            throw new UnauthorizedAccessException("Solo l'organizzatore dell'hackathon puo' eseguire questa operazione");
        }

        return hackathon;
    }

    private User getActiveUser(Long userId, String errorMessage) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(errorMessage));
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new ResourceNotFoundException(errorMessage);
        }
        return user;
    }

    private void startHackathonsReadyToRun(LocalDateTime now) {
        List<Hackathon> hackathonsToStart = hackathonRepository.findAll().stream()
                .filter(hackathon -> hackathon.getState() == HackathonState.IN_ISCRIZIONE)
                .filter(hackathon -> hackathon.getStartDate() != null)
                .filter(hackathon -> !hackathon.getStartDate().isAfter(now))
                .filter(hackathon -> hackathon.getEndDate() == null || hackathon.getEndDate().isAfter(now))
                .toList();

        hackathonsToStart.forEach(hackathon -> hackathon.setState(HackathonState.IN_CORSO));
        hackathonRepository.saveAll(hackathonsToStart);
    }

    private void moveExpiredHackathonsToEvaluation(LocalDateTime now) {
        List<Hackathon> hackathonsToEvaluate = hackathonRepository.findAll().stream()
                .filter(hackathon -> hackathon.getState() != HackathonState.CONCLUSO)
                .filter(hackathon -> hackathon.getState() != HackathonState.IN_VALUTAZIONE)
                .filter(hackathon -> hackathon.getEndDate() != null)
                .filter(hackathon -> !hackathon.getEndDate().isAfter(now))
                .toList();

        hackathonsToEvaluate.forEach(hackathon -> hackathon.setState(HackathonState.IN_VALUTAZIONE));
        hackathonRepository.saveAll(hackathonsToEvaluate);
    }

    private HackathonResponse mapToResponse(Hackathon hackathon) {
        User organizer = hackathonRoleAssignmentService.getOrganizer(hackathon);
        User judge = hackathonRoleAssignmentService.getJudge(hackathon);
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
                .organizerName(organizer != null ? organizer.getName() + " " + organizer.getSurname() : "N/A")
                .mentorIds(hackathonRoleAssignmentService.getMentors(hackathon).stream()
                        .map(User::getId)
                        .collect(Collectors.toSet()))
                .judgeId(judge != null ? judge.getId() : null)
                .judgeName(judge != null ? judge.getName() + " " + judge.getSurname() : null)
                .teamIds(hackathon.getTeams().stream()
                        .map(Team::getId)
                        .collect(Collectors.toSet()))
                .winningTeamId(hackathon.getTeamWinner() != null ? hackathon.getTeamWinner().getId() : null)
                .winningTeamName(hackathon.getTeamWinner() != null ? hackathon.getTeamWinner().getName() : null)
                .build();
    }
}
