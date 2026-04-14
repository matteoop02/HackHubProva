package unicam.ids.HackHub.dto.responses;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MentorAvailabilitySlotResponse(
        Long id,
        Long mentorId,
        Long hackathonId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String notes,
        boolean booked
) {
}
