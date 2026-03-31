package unicam.ids.HackHub.dto.requests.invite;

import jakarta.validation.constraints.NotNull;

public record RejectInsideInviteRequest(
    @NotNull
    Long inviteId
) {}
