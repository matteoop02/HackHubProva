package unicam.ids.HackHub.dto.responses;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record SubmissionResponse(
        Long id,
        String title,
        String content,
        LocalDateTime submittedAt,
        Long teamId,
        String teamName
) {}
