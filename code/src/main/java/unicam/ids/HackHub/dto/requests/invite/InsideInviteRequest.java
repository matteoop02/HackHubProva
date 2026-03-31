package unicam.ids.HackHub.dto.requests.invite;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record InsideInviteRequest (
        @NotNull(message = "L'id del destinatario è obbligatorio")
        Long recipientId,

        @NotNull(message = "L'id del ruolo proposto è obbligatorio")
        Long proposedRoleId,

        String message
) {}