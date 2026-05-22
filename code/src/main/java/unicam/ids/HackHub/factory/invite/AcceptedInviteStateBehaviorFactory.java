package unicam.ids.HackHub.factory.invite;

import unicam.ids.HackHub.model.state.invite.AcceptedInviteState;
import unicam.ids.HackHub.model.state.invite.InviteStateBehavior;

public class AcceptedInviteStateBehaviorFactory implements InviteStateBehaviorFactory {
    @Override
    public InviteStateBehavior createBehavior() {
        return new AcceptedInviteState();
    }
}
