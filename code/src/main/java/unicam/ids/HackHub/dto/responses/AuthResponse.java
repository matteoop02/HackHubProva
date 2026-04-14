package unicam.ids.HackHub.dto.responses;

import lombok.Builder;

import java.util.List;

@Builder
public record AuthResponse(
        String token,
        UserResponse user,
        List<String> actors
) {
}
