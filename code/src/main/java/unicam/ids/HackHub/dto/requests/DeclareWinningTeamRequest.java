package unicam.ids.HackHub.dto.requests;

import jakarta.validation.constraints.NotNull;

public record DeclareWinningTeamRequest(
        @NotNull(message = "Il team vincitore e' obbligatorio")
        Long teamId
) {
}
