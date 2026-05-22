package unicam.ids.HackHub.factory.invite;

import unicam.ids.HackHub.model.state.invite.ExpiredInviteState;
import unicam.ids.HackHub.model.state.invite.InviteStateBehavior;

public class ExpiredInviteStateBehaviorFactory implements InviteStateBehaviorFactory {
    @Override
    public InviteStateBehavior createBehavior() {
        return new ExpiredInviteState();
    }
}
