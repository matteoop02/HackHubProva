package unicam.ids.HackHub.dto.requests;

import jakarta.validation.constraints.NotNull;

public record AcceptInsideInviteRequest(
        @NotNull(message = "L'id dell'invito e' obbligatorio")
        Long inviteId
) {
}
