package unicam.ids.HackHub.dto.requests;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateMentorAvailabilitySlotRequest(
        @NotNull(message = "L'hackathon e' obbligatorio")
        Long hackathonId,

        @NotNull(message = "L'inizio dello slot e' obbligatorio")
        @Future(message = "L'inizio dello slot deve essere futuro")
        LocalDateTime startTime,

        @NotNull(message = "La fine dello slot e' obbligatoria")
        @Future(message = "La fine dello slot deve essere futura")
        LocalDateTime endTime,

        @Size(max = 500, message = "Le note non possono superare 500 caratteri")
        String notes
) {
}
