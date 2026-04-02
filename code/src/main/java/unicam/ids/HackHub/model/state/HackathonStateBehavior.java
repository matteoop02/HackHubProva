package unicam.ids.HackHub.model.state;

import unicam.ids.HackHub.model.Hackathon;

public interface HackathonStateBehavior {

    default void start(Hackathon hackathon) {
        throw new IllegalStateException("Impossibile avviare l'hackathon nello stato attuale: " + hackathon.getState());
    }

    default void closeSubscriptions(Hackathon hackathon) {
        throw new IllegalStateException("Impossibile terminare le iscrizioni nello stato attuale: " + hackathon.getState());
    }
}
