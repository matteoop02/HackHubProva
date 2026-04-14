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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final HackathonRepository hackathonRepository;
    private final unicam.ids.HackHub.repository.RuleViolationRepository ruleViolationRepository;
    private final EmailService emailService;

    public TeamService(TeamRepository teamRepository, UserRepository userRepository,
            HackathonRepository hackathonRepository,
            unicam.ids.HackHub.repository.RuleViolationRepository ruleViolationRepository,
            EmailService emailService) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.hackathonRepository = hackathonRepository;
        this.ruleViolationRepository = ruleViolationRepository;
        this.emailService = emailService;
    }

    public TeamResponse createTeam(Authentication authentication, CreateTeamRequest request) {
        User creator = userRepository.findByUsernameAndIsDeletedFalse(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato o eliminato"));

        if (creator.getTeam() != null) {
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
                .teamLeader(creator)
                .members(new ArrayList<>(List.of(creator)))
                .build();

        Team savedTeam = teamRepository.save(team);
        creator.setTeam(savedTeam);
        userRepository.save(creator);

        if (hackathon != null) {
            hackathon.getTeams().add(savedTeam);
            hackathonRepository.save(hackathon);
        }

        return mapToResponse(savedTeam);
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
                .mentorIds(team.getMentors().stream().map(User::getId).collect(Collectors.toList()))
                .build();
    }

    public void subscribeTeamToHackathon(Authentication authentication, Long teamId, Long hackathonId) {
        User user = userRepository.findByUsernameAndIsDeletedFalse(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato o eliminato"));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team non trovato"));

        if (!team.getTeamLeader().getId().equals(user.getId())) {
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

        if (team.getMembers().size() > hackathon.getMaxTeamSize()) {
            throw new BusinessLogicException("Il team supera il numero massimo di membri consentito per questo hackathon");
        }

        team.setHackathon(hackathon);
        hackathon.getTeams().add(team);
        teamRepository.save(team);
        hackathonRepository.save(hackathon);
    }

    public void reportViolation(org.springframework.security.core.Authentication authentication, Long teamId, unicam.ids.HackHub.dto.requests.team.ReportViolationRequest request) {
        User mentor = userRepository.findByUsernameAndIsDeletedFalse(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato o eliminato"));

        if (!"MENTOR".equals(mentor.getRole().getName())) {
            throw new unicam.ids.HackHub.exceptions.UnauthorizedAccessException("Solo i mentori possono segnalare violazioni");
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team non trovato"));

        if (team.getHackathon() == null) {
            throw new BusinessLogicException("Questo team non è iscritto a nessun hackathon");
        }

        boolean isMentorOfTeam = team.getMentors().stream().anyMatch(m -> m.getId().equals(mentor.getId()));
        boolean isMentorOfHackathon = team.getHackathon().getMentors().stream().anyMatch(m -> m.getId().equals(mentor.getId()));

        if (!isMentorOfTeam && !isMentorOfHackathon) {
            throw new unicam.ids.HackHub.exceptions.UnauthorizedAccessException("Non sei assegnato come mentore per questo team o hackathon");
        }

        unicam.ids.HackHub.model.RuleViolationReport violation = unicam.ids.HackHub.model.RuleViolationReport.builder()
                .team(team)
                .mentor(mentor)
                .description(request.description())
                .createdAt(java.time.LocalDateTime.now())
                .build();
        
        ruleViolationRepository.save(violation);

        User organizer = team.getHackathon().getOrganizer();
        String emailBody = "Il mentore " + mentor.getName() + " " + mentor.getSurname() + 
                " ha segnalato una violazione delle regole da parte del team '" + team.getName() + 
                "' nell'hackathon '" + team.getHackathon().getName() + "'.\n" +
                "Descrizione: " + request.description();

        emailService.sendEmail(organizer.getEmail(), "Segnalazione Violazione Regole - Team " + team.getName(), emailBody);
    }
}
