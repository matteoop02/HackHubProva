package unicam.ids.HackHub.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendSubmissionRequest(
        @NotNull(message = "L'id del team e' obbligatorio")
        Long teamId,

        @NotBlank(message = "Il titolo e' obbligatorio")
        String title,

        @NotBlank(message = "Il contenuto e' obbligatorio")
        String content
) {
}
