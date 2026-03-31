package unicam.ids.HackHub.dto.requests.invite;

import jakarta.validation.constraints.NotEmpty;

public record RejectOutsideInviteRequest(
    @NotEmpty
    String token
) {}
