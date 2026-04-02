package unicam.ids.HackHub.factory;

import unicam.ids.HackHub.enums.HackathonState;
import unicam.ids.HackHub.model.state.*;

public class HackathonStateFactory {
    public static HackathonStateBehavior from(HackathonState state) {
        return switch (state) {
            case IN_ISCRIZIONE -> new RegistrationStateBehavior();
            case IN_CORSO -> new RunningStateBehavior();
            case IN_VALUTAZIONE -> new EvaluationStateBehavior();
            case CONCLUSO -> new CompletedStateBehavior();
        };
    }
}
