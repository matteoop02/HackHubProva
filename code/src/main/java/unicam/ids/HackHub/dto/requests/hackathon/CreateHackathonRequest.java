package unicam.ids.HackHub.dto.requests.hackathon;

import jakarta.validation.constraints.*;
import unicam.ids.HackHub.validation.ChronologicalDates;

import java.time.LocalDateTime;

@ChronologicalDates
public record CreateHackathonRequest(
        @NotEmpty(message = "Nome obbligatorio")
        String name,

        @NotEmpty(message = "Luogo obbligatorio")
        String place,

        @NotEmpty(message = "Regolamento obbligatorio")
        String regulation,

        @NotNull(message = "Limite invio sottomissione obbligatorio")
        LocalDateTime subscriptionDeadline,

        @NotNull(message = "Data inizio obbligatoria")
        LocalDateTime startDate,

        @NotNull(message = "Data fine obbligatoria")
        LocalDateTime endDate,

        @Positive(message = "Importo del premio deve essere positivo")
        double reward,

        @Positive(message = "Massima dimensione dei team deve essere positiva")
        int maxTeamSize,

        boolean isPublic
) {}

