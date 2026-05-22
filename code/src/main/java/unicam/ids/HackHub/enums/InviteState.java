package unicam.ids.HackHub.enums;

import unicam.ids.HackHub.factory.invite.AcceptedInviteStateBehaviorFactory;
import unicam.ids.HackHub.factory.invite.ExpiredInviteStateBehaviorFactory;
import unicam.ids.HackHub.factory.invite.InviteStateBehaviorFactory;
import unicam.ids.HackHub.factory.invite.PendingInviteStateBehaviorFactory;
import unicam.ids.HackHub.factory.invite.RejectedInviteStateBehaviorFactory;
import unicam.ids.HackHub.model.state.invite.InviteStateBehavior;

public enum InviteState {
    IN_ATTESA(new PendingInviteStateBehaviorFactory()),
    ACCETTATO(new AcceptedInviteStateBehaviorFactory()),
    RIFIUTATO(new RejectedInviteStateBehaviorFactory()),
    SCADUTO(new ExpiredInviteStateBehaviorFactory());

    private final InviteStateBehaviorFactory behaviorFactory;

    InviteState(InviteStateBehaviorFactory behaviorFactory) {
        this.behaviorFactory = behaviorFactory;
    }

    public InviteStateBehavior createBehavior() {
        return behaviorFactory.createBehavior();
    }
}
