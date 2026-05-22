package unicam.ids.HackHub.factory.hackathon;

import unicam.ids.HackHub.model.state.HackathonStateBehavior;
import unicam.ids.HackHub.model.state.RegistrationStateBehavior;

public class RegistrationStateBehaviorFactory implements HackathonStateBehaviorFactory {
    @Override
    public HackathonStateBehavior createBehavior() {
        return new RegistrationStateBehavior();
    }
}
