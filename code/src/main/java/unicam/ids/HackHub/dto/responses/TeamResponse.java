package unicam.ids.HackHub.dto.responses;

import lombok.Builder;
import java.util.List;

@Builder
public record TeamResponse(
        Long id,
        String name,
        boolean isPublic,
        Long hackathonId,
        Long leaderId,
        List<Long> memberIds
) {}
