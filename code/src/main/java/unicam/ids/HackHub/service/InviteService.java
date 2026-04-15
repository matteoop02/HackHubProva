package unicam.ids.HackHub.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import unicam.ids.HackHub.dto.requests.AcceptInsideInviteRequest;
import unicam.ids.HackHub.dto.requests.invite.InsideInviteRequest;
import unicam.ids.HackHub.dto.requests.invite.RejectInsideInviteRequest;
import unicam.ids.HackHub.dto.requests.invite.RejectOutsideInviteRequest;
import unicam.ids.HackHub.enums.InviteState;
import unicam.ids.HackHub.enums.TeamRole;
import unicam.ids.HackHub.model.InviteInsidePlatform;
import unicam.ids.HackHub.model.InviteOutsidePlatform;
import unicam.ids.HackHub.model.User;
import unicam.ids.HackHub.model.UserRole;
import unicam.ids.HackHub.repository.InsideInviteRepository;
import unicam.ids.HackHub.repository.OutsideInviteRepository;
import unicam.ids.HackHub.repository.UserRepository;
import unicam.ids.HackHub.repository.UserRoleRepository;

import unicam.ids.HackHub.dto.responses.InviteResponse;
import unicam.ids.HackHub.exceptions.BusinessLogicException;
import unicam.ids.HackHub.exceptions.ResourceNotFoundException;
import unicam.ids.HackHub.exceptions.UnauthorizedAccessException;
import unicam.ids.HackHub.model.Hackathon;
import unicam.ids.HackHub.model.Team;

@Service
public class InviteService {

    private final InsideInviteRepository insideInviteRepository;
    private final OutsideInviteRepository outsideInviteRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final EmailService emailService;
    private final TeamMembershipService teamMembershipService;

    public InviteService(InsideInviteRepository insideInviteRepository, 
                         OutsideInviteRepository outsideInviteRepository,
                         UserRepository userRepository,
                         UserRoleRepository userRoleRepository,
                         EmailService emailService,
                         TeamMembershipService teamMembershipService) {
        this.insideInviteRepository = insideInviteRepository;
        this.outsideInviteRepository = outsideInviteRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.emailService = emailService;
        this.teamMembershipService = teamMembershipService;
    }

    public void rejectOutsideInvite(RejectOutsideInviteRequest request) {
        InviteOutsidePlatform invite = outsideInviteRepository.findByInviteToken(request.token())
                .orElseThrow(() -> new ResourceNotFoundException("Invito non trovato"));
        invite.reject();
        outsideInviteRepository.save(invite);
    }

    public InviteResponse inviteUserToTeam(Authentication authentication, InsideInviteRequest request) {
        User sender = userRepository.findByUsernameAndIsDeletedFalse(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Mittente non trovato"));
        
        User recipient = userRepository.findById(request.recipientId())
                .orElseThrow(() -> new ResourceNotFoundException("Destinatario non trovato"));

        Team senderTeam = teamMembershipService.getCurrentTeam(sender);
        if (senderTeam == null || !teamMembershipService.isLeader(sender, senderTeam)) {
            throw new BusinessLogicException("Devi essere il leader del tuo team per invitare altri utenti");
        }
        
        UserRole role = userRoleRepository.findById(request.proposedRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Ruolo proposto non trovato"));

        InviteInsidePlatform invite = InviteInsidePlatform.builder()
                .senderUser(sender)
                .recipientUser(recipient)
                .team(senderTeam)
                .proposedRole(role)
                .message(request.message())
                .status(InviteState.IN_ATTESA)
                .expiresAt(java.time.LocalDateTime.now().plusDays(7))
                .build();
        
        return mapToResponse(insideInviteRepository.save(invite));
    }

    private InviteResponse mapToResponse(InviteInsidePlatform invite) {
        return InviteResponse.builder()
                .id(invite.getId())
                .senderId(invite.getSenderUser() != null ? invite.getSenderUser().getId() : null)
                .recipientId(invite.getRecipientUser() != null ? invite.getRecipientUser().getId() : null)
                .teamId(invite.getTeam() != null ? invite.getTeam().getId() : null)
                .proposedRoleName(invite.getProposedRole() != null ? invite.getProposedRole().getName() : null)
                .message(invite.getMessage())
                .status(invite.getStatus())
                .expiresAt(invite.getExpiresAt())
                .build();
    }

    public void rejectTeamInvite(Authentication authentication, RejectInsideInviteRequest request) {
        InviteInsidePlatform invite = insideInviteRepository.findById(request.inviteId())
                .orElseThrow(() -> new ResourceNotFoundException("Invito interno non trovato"));
        
        if (!invite.getRecipientUser().getUsername().equals(authentication.getName())) {
            throw new UnauthorizedAccessException("Non sei l'utente destinatario di questo invito");
        }
        
        invite.reject();
        insideInviteRepository.save(invite);
    }

    public InviteResponse acceptTeamInvite(Authentication authentication, AcceptInsideInviteRequest request) {
        InviteInsidePlatform invite = insideInviteRepository.findById(request.inviteId())
                .orElseThrow(() -> new ResourceNotFoundException("Invito interno non trovato"));

        User recipient = userRepository.findByUsernameAndIsDeletedFalse(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));

        if (!invite.getRecipientUser().getId().equals(recipient.getId())) {
            throw new UnauthorizedAccessException("Non sei l'utente destinatario di questo invito");
        }

        if (invite.isExpired()) {
            invite.setStatus(InviteState.SCADUTO);
            insideInviteRepository.save(invite);
            throw new BusinessLogicException("L'invito e' scaduto");
        }

        if (teamMembershipService.hasTeam(recipient)) {
            throw new BusinessLogicException("L'utente appartiene gia' a un team");
        }

        Team team = invite.getTeam();
        Hackathon hackathon = team.getHackathon();
        if (hackathon != null && teamMembershipService.countMembers(team) >= hackathon.getMaxTeamSize()) {
            throw new BusinessLogicException("Il team ha gia' raggiunto il numero massimo di membri");
        }

        invite.accept();
        teamMembershipService.addMembership(recipient, team, TeamRole.MEMBER);

        insideInviteRepository.save(invite);

        return mapToResponse(invite);
    }

    public void createOutsideInvite(Authentication authentication, unicam.ids.HackHub.dto.requests.invite.CreateOutsideInviteRequest request) {
        User sender = userRepository.findByUsernameAndIsDeletedFalse(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Mittente non trovato"));

        String token = java.util.UUID.randomUUID().toString();

        InviteOutsidePlatform invite = InviteOutsidePlatform.builder()
                .senderUser(sender)
                .recipientEmail(request.recipientEmail())
                .inviteToken(token)
                .message(request.message())
                .status(InviteState.IN_ATTESA)
                .expiresAt(java.time.LocalDateTime.now().plusDays(7))
                .build();
        
        outsideInviteRepository.save(invite);
        
        String emailBody = "Sei stato invitato da " + sender.getName() + " " + sender.getSurname() + " a iscriverti alla piattaforma HackHub.\n\n";
        if (request.message() != null && !request.message().isBlank()) {
            emailBody += "Messaggio: " + request.message() + "\n\n";
        }
        emailBody += "Usa questo token per registrarti: " + token;
        
        emailService.sendEmail(request.recipientEmail(), "Invito a iscriverti ad HackHub", emailBody);
    }
}
