package unicam.ids.HackHub.dto.responses;

import lombok.Builder;
import unicam.ids.HackHub.enums.HackathonState;
import java.time.LocalDateTime;

@Builder
public record HackathonResponse(
        Long id,
        String name,
        String place,
        String regulation,
        LocalDateTime subscriptionDeadline,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Double reward,
        Integer maxTeamSize,
        boolean isPublic,
        HackathonState state,
        String organizerName
) {}
