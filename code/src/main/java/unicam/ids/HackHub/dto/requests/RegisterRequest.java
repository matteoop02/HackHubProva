package unicam.ids.HackHub.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterRequest(
        @Schema(example = "Mario")
        @NotBlank(message = "Il nome e' obbligatorio")
        String name,

        @Schema(example = "Rossi")
        @NotBlank(message = "Il cognome e' obbligatorio")
        String surname,

        @Schema(example = "mario.rossi")
        @NotBlank(message = "Lo username e' obbligatorio")
        String username,

        @Schema(example = "mario.rossi@example.com")
        @Email(message = "Email non valida")
        @NotBlank(message = "L'email e' obbligatoria")
        String email,

        @Schema(example = "Password123!")
        @Size(min = 8, message = "La password deve contenere almeno 8 caratteri")
        String password,

        @Schema(example = "1999-05-14")
        @Past(message = "La data di nascita deve essere nel passato")
        LocalDate dateOfBirth,

        @Schema(
                example = "ORGANIZER",
                description = "Ruolo opzionale. Esempi accettati: ORGANIZER/ORGANIZZATORE, JUDGE/GIUDICE, MENTOR, REGISTERED_USER/REGISTERED/USER. Se omesso, viene usato UTENTE_REGISTRATO.")
        String roleName
) {
}
