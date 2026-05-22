package unicam.ids.HackHub.enums;

import unicam.ids.HackHub.factory.hackathon.CompletedStateBehaviorFactory;
import unicam.ids.HackHub.factory.hackathon.EvaluationStateBehaviorFactory;
import unicam.ids.HackHub.factory.hackathon.HackathonStateBehaviorFactory;
import unicam.ids.HackHub.factory.hackathon.RegistrationStateBehaviorFactory;
import unicam.ids.HackHub.factory.hackathon.RunningStateBehaviorFactory;
import unicam.ids.HackHub.model.state.HackathonStateBehavior;

public enum HackathonState {
    IN_ISCRIZIONE(new RegistrationStateBehaviorFactory()),
    IN_CORSO(new RunningStateBehaviorFactory()),
    IN_VALUTAZIONE(new EvaluationStateBehaviorFactory()),
    CONCLUSO(new CompletedStateBehaviorFactory());

    private final HackathonStateBehaviorFactory behaviorFactory;

    HackathonState(HackathonStateBehaviorFactory behaviorFactory) {
        this.behaviorFactory = behaviorFactory;
    }

    public HackathonStateBehavior createBehavior() {
        return behaviorFactory.createBehavior();
    }
}
