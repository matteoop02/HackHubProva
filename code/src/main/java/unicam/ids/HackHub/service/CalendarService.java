package unicam.ids.HackHub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import unicam.ids.HackHub.dto.requests.BookSupportCallSlotRequest;
import unicam.ids.HackHub.dto.requests.CreateMentorAvailabilitySlotRequest;
import unicam.ids.HackHub.dto.requests.ProposeSupportCallRequest;
import unicam.ids.HackHub.dto.responses.MentorAvailabilitySlotResponse;
import unicam.ids.HackHub.dto.responses.SupportCallProposalResponse;
import unicam.ids.HackHub.enums.HackathonRole;
import unicam.ids.HackHub.exceptions.BusinessLogicException;
import unicam.ids.HackHub.exceptions.ResourceNotFoundException;
import unicam.ids.HackHub.exceptions.UnauthorizedAccessException;
import unicam.ids.HackHub.model.Hackathon;
import unicam.ids.HackHub.model.MentorAvailabilitySlot;
import unicam.ids.HackHub.model.SupportCallProposal;
import unicam.ids.HackHub.model.Team;
import unicam.ids.HackHub.model.User;
import unicam.ids.HackHub.repository.HackathonRepository;
import unicam.ids.HackHub.repository.MentorAvailabilitySlotRepository;
import unicam.ids.HackHub.repository.SupportCallProposalRepository;
import unicam.ids.HackHub.repository.TeamRepository;
import unicam.ids.HackHub.repository.UserRepository;
import unicam.ids.HackHub.util.RoleNames;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final UserRepository userRepository;
    private final HackathonRepository hackathonRepository;
    private final TeamRepository teamRepository;
    private final MentorAvailabilitySlotRepository mentorAvailabilitySlotRepository;
    private final SupportCallProposalRepository supportCallProposalRepository;
    private final EmailService emailService;
    private final TeamMembershipService teamMembershipService;
    private final HackathonRoleAssignmentService hackathonRoleAssignmentService;

    public MentorAvailabilitySlotResponse createAvailabilitySlot(Authentication authentication,
            CreateMentorAvailabilitySlotRequest request) {
        User mentor = getAuthenticatedUser(authentication);
        ensureMentor(mentor);

        Hackathon hackathon = hackathonRepository.findById(request.hackathonId())
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon non trovato"));

        if (!hackathonRoleAssignmentService.hasRole(mentor, hackathon, HackathonRole.MENTOR)) {
            throw new UnauthorizedAccessException("Il mentore non e' assegnato a questo hackathon");
        }

        validateChronology(request.startTime(), request.endTime());

        MentorAvailabilitySlot slot = MentorAvailabilitySlot.builder()
                .mentor(mentor)
                .hackathon(hackathon)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .notes(request.notes())
                .booked(false)
                .build();

        return mapSlot(mentorAvailabilitySlotRepository.save(slot));
    }

    public SupportCallProposalResponse proposeSupportCall(Authentication authentication,
            ProposeSupportCallRequest request) {
        User mentor = getAuthenticatedUser(authentication);
        ensureMentor(mentor);

        Team team = teamRepository.findById(request.teamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team non trovato"));

        if (team.getHackathon() == null) {
            throw new BusinessLogicException("Il team non e' iscritto a nessun hackathon");
        }

        Hackathon hackathon = team.getHackathon();
        boolean mentorAssigned = team.getMentors().stream().anyMatch(user -> Objects.equals(user.getId(), mentor.getId()))
                || hackathonRoleAssignmentService.hasRole(mentor, hackathon, HackathonRole.MENTOR);

        if (!mentorAssigned) {
            throw new UnauthorizedAccessException("Il mentore non e' assegnato al team o all'hackathon");
        }

        MentorAvailabilitySlot slot = null;
        if (request.slotId() != null) {
            slot = mentorAvailabilitySlotRepository.findById(request.slotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Slot non trovato"));

            if (!Objects.equals(slot.getMentor().getId(), mentor.getId())) {
                throw new UnauthorizedAccessException("Lo slot non appartiene al mentore autenticato");
            }
            if (!Objects.equals(slot.getHackathon().getId(), hackathon.getId())) {
                throw new BusinessLogicException("Lo slot selezionato non appartiene all'hackathon del team");
            }
            if (Boolean.TRUE.equals(slot.getBooked())) {
                throw new BusinessLogicException("Lo slot selezionato e' gia' stato impegnato");
            }
        }

        if (slot == null && (request.proposedStartTime() == null || request.proposedEndTime() == null)) {
            throw new BusinessLogicException("Occorre indicare uno slot disponibile oppure un intervallo proposto");
        }

        if (slot == null) {
            validateChronology(request.proposedStartTime(), request.proposedEndTime());
        }

        SupportCallProposal proposal = SupportCallProposal.builder()
                .mentor(mentor)
                .team(team)
                .hackathon(hackathon)
                .slot(slot)
                .subject(request.subject())
                .message(request.message())
                .proposedStartTime(slot != null ? slot.getStartTime() : request.proposedStartTime())
                .proposedEndTime(slot != null ? slot.getEndTime() : request.proposedEndTime())
                .status("PROPOSTA")
                .build();

        SupportCallProposal savedProposal = supportCallProposalRepository.save(proposal);

        if (slot != null) {
            slot.setBooked(true);
            mentorAvailabilitySlotRepository.save(slot);
        }

        User teamLeader = teamMembershipService.getLeader(team);
        if (teamLeader != null) {
            emailService.sendEmail(
                    teamLeader.getEmail(),
                    "Nuova proposta di call di supporto per il team " + team.getName(),
                    "Il mentore " + mentor.getName() + " " + mentor.getSurname()
                            + " ha proposto una call di supporto.\n"
                            + "Oggetto: " + request.subject() + "\n"
                            + "Messaggio: " + request.message() + "\n"
                            + "Quando: " + savedProposal.getProposedStartTime() + " - " + savedProposal.getProposedEndTime()
            );
        }

        return mapProposal(savedProposal);
    }

    public SupportCallProposalResponse bookSupportCallSlot(Authentication authentication, Long slotId,
            BookSupportCallSlotRequest request) {
        User leader = getAuthenticatedUser(authentication);
        Team team = teamMembershipService.getCurrentTeam(leader);

        if (team == null || !teamMembershipService.isLeader(leader, team)) {
            throw new UnauthorizedAccessException("Solo il leader del team puo' prenotare uno slot con il mentore");
        }
        if (team.getHackathon() == null) {
            throw new BusinessLogicException("Il team non e' iscritto a nessun hackathon");
        }

        MentorAvailabilitySlot slot = mentorAvailabilitySlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot non trovato"));

        if (!slot.getHackathon().getId().equals(team.getHackathon().getId())) {
            throw new BusinessLogicException("Lo slot non appartiene all'hackathon del team");
        }
        if (Boolean.TRUE.equals(slot.getBooked())) {
            throw new BusinessLogicException("Lo slot selezionato e' gia' stato prenotato");
        }

        SupportCallProposal proposal = SupportCallProposal.builder()
                .mentor(slot.getMentor())
                .team(team)
                .hackathon(team.getHackathon())
                .slot(slot)
                .subject(request.subject())
                .message(request.message())
                .proposedStartTime(slot.getStartTime())
                .proposedEndTime(slot.getEndTime())
                .status("PRENOTATA")
                .build();

        slot.setBooked(true);
        mentorAvailabilitySlotRepository.save(slot);
        SupportCallProposal savedProposal = supportCallProposalRepository.save(proposal);

        emailService.sendEmail(
                slot.getMentor().getEmail(),
                "Nuova prenotazione slot con il team " + team.getName(),
                "Il leader del team " + leader.getName() + " " + leader.getSurname()
                        + " ha prenotato lo slot " + slot.getStartTime() + " - " + slot.getEndTime()
                        + ".\nOggetto: " + request.subject() + "\nMessaggio: " + request.message()
        );

        return mapProposal(savedProposal);
    }

    private User getAuthenticatedUser(Authentication authentication) {
        return userRepository.findByUsernameAndIsDeletedFalse(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));
    }

    private void ensureMentor(User mentor) {
        if (!RoleNames.MENTOR.equals(mentor.getRole().getName())
                && !hackathonRoleAssignmentService.hasAnyAssignment(mentor, HackathonRole.MENTOR)) {
            throw new UnauthorizedAccessException("Solo i mentori possono eseguire questa operazione");
        }
    }

    private void validateChronology(LocalDateTime startTime, LocalDateTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new BusinessLogicException("La fine deve essere successiva all'inizio");
        }
    }

    private MentorAvailabilitySlotResponse mapSlot(MentorAvailabilitySlot slot) {
        return MentorAvailabilitySlotResponse.builder()
                .id(slot.getId())
                .mentorId(slot.getMentor().getId())
                .hackathonId(slot.getHackathon().getId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .notes(slot.getNotes())
                .booked(Boolean.TRUE.equals(slot.getBooked()))
                .build();
    }

    private SupportCallProposalResponse mapProposal(SupportCallProposal proposal) {
        return SupportCallProposalResponse.builder()
                .id(proposal.getId())
                .mentorId(proposal.getMentor().getId())
                .teamId(proposal.getTeam().getId())
                .hackathonId(proposal.getHackathon().getId())
                .slotId(proposal.getSlot() != null ? proposal.getSlot().getId() : null)
                .subject(proposal.getSubject())
                .message(proposal.getMessage())
                .proposedStartTime(proposal.getProposedStartTime())
                .proposedEndTime(proposal.getProposedEndTime())
                .status(proposal.getStatus())
                .build();
    }
}
