package unicam.ids.HackHub.model.state.invite;

import unicam.ids.HackHub.enums.InviteState;
import unicam.ids.HackHub.model.Invite;

import java.time.LocalDateTime;

public class PendingInviteState implements InviteStateBehavior {

    @Override
    public void send(Invite invite) {
        throw new IllegalStateException("L'invito è già stato inviato");
    }

    @Override
    public void accept(Invite invite) {
        if (LocalDateTime.now().isAfter(invite.getExpiresAt())) {
            invite.setStatus(InviteState.SCADUTO);
            throw new IllegalStateException("L'invito è scaduto il: " + invite.getExpiresAt());
        }
        invite.setStatus(InviteState.ACCETTATO);
    }

    @Override
    public void reject(Invite invite) {
        invite.setStatus(InviteState.RIFIUTATO);
    }
}
