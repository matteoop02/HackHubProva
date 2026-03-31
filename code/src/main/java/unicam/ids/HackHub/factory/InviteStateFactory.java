package unicam.ids.HackHub.factory;

import unicam.ids.HackHub.enums.InviteState;
import unicam.ids.HackHub.model.Invite;
import unicam.ids.HackHub.model.state.invite.*;

public class InviteStateFactory {
    public static InviteStateBehavior from(InviteState state) {
        return switch (state) {
            case IN_ATTESA -> new PendingInviteState();
            case ACCETTATO -> new AcceptedInviteState();
            case RIFIUTATO -> new RejectedInviteState();
            case SCADUTO -> new ExpiredInviteState();
        };
    }
}
