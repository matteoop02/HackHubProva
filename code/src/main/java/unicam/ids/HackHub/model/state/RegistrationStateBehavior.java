package unicam.ids.HackHub.model.state;

import unicam.ids.HackHub.enums.HackathonState;
import unicam.ids.HackHub.model.Hackathon;
import java.time.LocalDateTime;

public class RegistrationStateBehavior implements HackathonStateBehavior {

    @Override
    public void start(Hackathon hackathon) {
        if (LocalDateTime.now().isBefore(hackathon.getStartDate())) {
            throw new IllegalStateException("Non puoi avviare l'hackathon prima della data di inizio (" + hackathon.getStartDate() + ")");
        }
        hackathon.setState(HackathonState.IN_CORSO);
    }

    @Override
    public void closeSubscriptions(Hackathon hackathon) {
        if (LocalDateTime.now().isBefore(hackathon.getStartDate())) {
            throw new IllegalStateException("Non puoi terminare le iscrizioni prima della data di inizio dell'hackathon (" + hackathon.getStartDate() + ")");
        }
        hackathon.setState(HackathonState.IN_CORSO);
    }
}
