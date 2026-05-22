package unicam.ids.HackHub.factory.hackathon;

import unicam.ids.HackHub.model.state.HackathonStateBehavior;
import unicam.ids.HackHub.model.state.RunningStateBehavior;

public class RunningStateBehaviorFactory implements HackathonStateBehaviorFactory {
    @Override
    public HackathonStateBehavior createBehavior() {
        return new RunningStateBehavior();
    }
}
