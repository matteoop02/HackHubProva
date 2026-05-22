package unicam.ids.HackHub.factory.hackathon;

import unicam.ids.HackHub.model.state.CompletedStateBehavior;
import unicam.ids.HackHub.model.state.HackathonStateBehavior;

public class CompletedStateBehaviorFactory implements HackathonStateBehaviorFactory {
    @Override
    public HackathonStateBehavior createBehavior() {
        return new CompletedStateBehavior();
    }
}
