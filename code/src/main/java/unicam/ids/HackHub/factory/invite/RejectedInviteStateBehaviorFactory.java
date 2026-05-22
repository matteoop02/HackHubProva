package unicam.ids.HackHub.factory.invite;

import unicam.ids.HackHub.model.state.invite.InviteStateBehavior;
import unicam.ids.HackHub.model.state.invite.RejectedInviteState;

public class RejectedInviteStateBehaviorFactory implements InviteStateBehaviorFactory {
    @Override
    public InviteStateBehavior createBehavior() {
        return new RejectedInviteState();
    }
}
