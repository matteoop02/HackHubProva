package unicam.ids.HackHub.dto.responses;

import lombok.Builder;
import unicam.ids.HackHub.enums.InviteState;
import java.time.LocalDateTime;

@Builder
public record InviteResponse(
        Long id,
        Long senderId,
        Long recipientId,
        Long teamId,
        String proposedRoleName,
        String message,
        InviteState status,
        LocalDateTime expiresAt
) {}
