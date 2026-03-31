package unicam.ids.HackHub.dto.requests.submission;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

public record UpdateTeamSubmissionRequest(
        @NotNull(message = "L'ID della sottomissione è obbligatorio")
        Long submissionId,

        @NotEmpty(message = "Il titolo da modificare non può essere vuoto")
        String title,

        @NotEmpty(message = "Il contenuto da modificare non può essere vuoto")
        String content
) {}
