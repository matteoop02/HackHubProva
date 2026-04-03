package unicam.ids.HackHub.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import unicam.ids.HackHub.dto.requests.invite.InsideInviteRequest;
import unicam.ids.HackHub.dto.requests.invite.RejectInsideInviteRequest;
import unicam.ids.HackHub.dto.requests.invite.RejectOutsideInviteRequest;
import unicam.ids.HackHub.enums.InviteState;
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

@Service
public class InviteService {

    private final InsideInviteRepository insideInviteRepository;
    private final OutsideInviteRepository outsideInviteRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final EmailService emailService;

    public InviteService(InsideInviteRepository insideInviteRepository, 
                         OutsideInviteRepository outsideInviteRepository,
                         UserRepository userRepository,
                         UserRoleRepository userRoleRepository,
                         EmailService emailService) {
        this.insideInviteRepository = insideInviteRepository;
        this.outsideInviteRepository = outsideInviteRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.emailService = emailService;
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

        if (sender.getTeam() == null || sender.getTeam().getTeamLeader() == null || !sender.getTeam().getTeamLeader().getId().equals(sender.getId())) {
            throw new BusinessLogicException("Devi essere il leader del tuo team per invitare altri utenti");
        }
        
        UserRole role = userRoleRepository.findById(request.proposedRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Ruolo proposto non trovato"));

        InviteInsidePlatform invite = InviteInsidePlatform.builder()
                .senderUser(sender)
                .recipientUser(recipient)
                .team(sender.getTeam())
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