package unicam.ids.HackHub.dto.responses;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SupportCallProposalResponse(
        Long id,
        Long mentorId,
        Long teamId,
        Long hackathonId,
        Long slotId,
        String subject,
        String message,
        LocalDateTime proposedStartTime,
        LocalDateTime proposedEndTime,
        String status
) {
}
