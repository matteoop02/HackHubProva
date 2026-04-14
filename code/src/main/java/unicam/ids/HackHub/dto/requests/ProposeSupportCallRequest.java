package unicam.ids.HackHub.dto.requests;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ProposeSupportCallRequest(
        @NotNull(message = "Il team e' obbligatorio")
        Long teamId,

        Long slotId,

        @Future(message = "L'inizio proposto deve essere futuro")
        LocalDateTime proposedStartTime,

        @Future(message = "La fine proposta deve essere futura")
        LocalDateTime proposedEndTime,

        @NotBlank(message = "L'oggetto della call e' obbligatorio")
        @Size(max = 255, message = "L'oggetto non puo' superare 255 caratteri")
        String subject,

        @NotBlank(message = "Il messaggio della proposta e' obbligatorio")
        @Size(max = 2000, message = "Il messaggio non puo' superare 2000 caratteri")
        String message
) {
}
