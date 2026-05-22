package unicam.ids.HackHub.factory.hackathon;

import unicam.ids.HackHub.model.state.EvaluationStateBehavior;
import unicam.ids.HackHub.model.state.HackathonStateBehavior;

public class EvaluationStateBehaviorFactory implements HackathonStateBehaviorFactory {
    @Override
    public HackathonStateBehavior createBehavior() {
        return new EvaluationStateBehavior();
    }
}
