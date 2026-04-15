package unicam.ids.HackHub.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import unicam.ids.HackHub.dto.requests.team.CreateTeamRequest;
import unicam.ids.HackHub.dto.responses.TeamResponse;
import unicam.ids.HackHub.enums.HackathonRole;
import unicam.ids.HackHub.enums.TeamRole;
import unicam.ids.HackHub.exceptions.BusinessLogicException;
import unicam.ids.HackHub.exceptions.ResourceNotFoundException;
import unicam.ids.HackHub.exceptions.UnauthorizedAccessException;
import unicam.ids.HackHub.model.Hackathon;
import unicam.ids.HackHub.model.RuleViolationReport;
import unicam.ids.HackHub.model.Team;
import unicam.ids.HackHub.model.User;
import unicam.ids.HackHub.repository.HackathonRepository;
import unicam.ids.HackHub.repository.RuleViolationRepository;
import unicam.ids.HackHub.repository.TeamRepository;
import unicam.ids.HackHub.repository.UserRepository;
import unicam.ids.HackHub.util.RoleNames;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final HackathonRepository hackathonRepository;
    private final RuleViolationRepository ruleViolationRepository;
    private final EmailService emailService;
    private final TeamMembershipService teamMembershipService;
    private final HackathonRoleAssignmentService hackathonRoleAssignmentService;

    public TeamService(TeamRepository teamRepository, UserRepository userRepository,
            HackathonRepository hackathonRepository, RuleViolationRepository ruleViolationRepository,
            EmailService emailService, TeamMembershipService teamMembershipService,
            HackathonRoleAssignmentService hackathonRoleAssignmentService) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.hackathonRepository = hackathonRepository;
        this.ruleViolationRepository = ruleViolationRepository;
        this.emailService = emailService;
        this.teamMembershipService = teamMembershipService;
        this.hackathonRoleAssignmentService = hackathonRoleAssignmentService;
    }

    public TeamResponse createTeam(Authentication authentication, CreateTeamRequest request) {
        User creator = userRepository.findByUsernameAndIsDeletedFalse(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato o eliminato"));

        if (teamMembershipService.hasTeam(creator)) {
            throw new BusinessLogicException("L'utente appartiene gia' a un team");
        }

        Hackathon hackathon = null;
        if (request.hackathonId() != null) {
            hackathon = hackathonRepository.findById(request.hackathonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hackathon non trovato"));
        }

        Optional<Team> teamOptional = teamRepository.findByName(request.name());
        if(teamOptional.isPresent()) {
            throw new IllegalArgumentException("Nome team già esistente");
        }

        Team team = Team.builder()
                .name(request.name())
                .isPublic(request.isPublic())
                .hackathon(hackathon)
                .build();

        Team savedTeam = teamRepository.save(team);
        teamMembershipService.addMembership(creator, savedTeam, TeamRole.LEADER);

        if (hackathon != null) {
            hackathon.getTeams().add(savedTeam);
            hackathonRepository.save(hackathon);
        }

        return mapToResponse(savedTeam);
    }

    public void leaveTeam(Authentication authentication) {
        User user = userRepository.findByUsernameAndIsDeletedFalse(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato o eliminato"));

        Team team = teamMembershipService.getCurrentTeam(user);
        if (team == null) {
            throw new BusinessLogicException("L'utente non fa parte di alcun team");
        }

        if (teamMembershipService.isLeader(user, team)) {
            throw new BusinessLogicException(
                    "Il leader non può lasciare il team: elimina il team o nomina un altro leader");
        }

        teamMembershipService.removeMembership(user, team);
    }

    private TeamResponse mapToResponse(Team team) {
        User leader = teamMembershipService.getLeader(team);
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .isPublic(team.isPublic())
                .hackathonId(team.getHackathon() != null ? team.getHackathon().getId() : null)
                .leaderId(leader != null ? leader.getId() : null)
                .memberIds(teamMembershipService.getMemberIds(team))
                .mentorIds(team.getMentors().stream().map(User::getId).toList())
                .build();
    }

    public void subscribeTeamToHackathon(Authentication authentication, Long teamId, Long hackathonId) {
        User user = userRepository.findByUsernameAndIsDeletedFalse(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato o eliminato"));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team non trovato"));

        if (!teamMembershipService.isLeader(user, team)) {
            throw new BusinessLogicException("Solo il leader può iscrivere il team a un hackathon");
        }

        if (team.getHackathon() != null) {
            throw new BusinessLogicException("Il team è già iscritto a un hackathon");
        }

        Hackathon hackathon = hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon non trovato"));

        if (hackathon.getState() != unicam.ids.HackHub.enums.HackathonState.IN_ISCRIZIONE) {
            throw new BusinessLogicException("L'hackathon non accetta più iscrizioni");
        }

        if (teamMembershipService.countMembers(team) > hackathon.getMaxTeamSize()) {
            throw new BusinessLogicException("Il team supera il numero massimo di membri consentito per questo hackathon");
        }

        team.setHackathon(hackathon);
        hackathon.getTeams().add(team);
        teamRepository.save(team);
        hackathonRepository.save(hackathon);
    }

    public void reportViolation(Authentication authentication, Long teamId,
            unicam.ids.HackHub.dto.requests.team.ReportViolationRequest request) {
        User mentor = userRepository.findByUsernameAndIsDeletedFalse(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato o eliminato"));

        if (!RoleNames.MENTOR.equals(mentor.getRole().getName())) {
            throw new UnauthorizedAccessException("Solo i mentori possono segnalare violazioni");
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team non trovato"));

        if (team.getHackathon() == null) {
            throw new BusinessLogicException("Questo team non è iscritto a nessun hackathon");
        }

        boolean isMentorOfTeam = team.getMentors().stream().anyMatch(m -> m.getId().equals(mentor.getId()));
        boolean isMentorOfHackathon = hackathonRoleAssignmentService.hasRole(mentor, team.getHackathon(), HackathonRole.MENTOR);

        if (!isMentorOfTeam && !isMentorOfHackathon) {
            throw new UnauthorizedAccessException("Non sei assegnato come mentore per questo team o hackathon");
        }

        RuleViolationReport violation = RuleViolationReport.builder()
                .team(team)
                .mentor(mentor)
                .description(request.description())
                .createdAt(LocalDateTime.now())
                .build();
        
        ruleViolationRepository.save(violation);

        User organizer = hackathonRoleAssignmentService.getOrganizer(team.getHackathon());
        String emailBody = "Il mentore " + mentor.getName() + " " + mentor.getSurname() + 
                " ha segnalato una violazione delle regole da parte del team '" + team.getName() + 
                "' nell'hackathon '" + team.getHackathon().getName() + "'.\n" +
                "Descrizione: " + request.description();

        emailService.sendEmail(organizer.getEmail(), "Segnalazione Violazione Regole - Team " + team.getName(), emailBody);
    }
}
