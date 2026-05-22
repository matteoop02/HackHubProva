package unicam.ids.HackHub.factory.invite;

import unicam.ids.HackHub.model.state.invite.InviteStateBehavior;
import unicam.ids.HackHub.model.state.invite.PendingInviteState;

public class PendingInviteStateBehaviorFactory implements InviteStateBehaviorFactory {
    @Override
    public InviteStateBehavior createBehavior() {
        return new PendingInviteState();
    }
}
