package unicam.ids.HackHub.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(example = "mario.rossi")
        @NotBlank(message = "Lo username e' obbligatorio")
        String username,

        @Schema(example = "Password123!")
        @NotBlank(message = "La password e' obbligatoria")
        String password
) {
}
