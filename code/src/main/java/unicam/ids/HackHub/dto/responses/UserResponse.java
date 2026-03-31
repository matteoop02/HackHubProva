package unicam.ids.HackHub.dto.responses;

import lombok.Builder;

@Builder
public record UserResponse(
        Long id,
        String name,
        String surname,
        String username,
        String email,
        String roleName
) {}
