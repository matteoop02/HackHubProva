package unicam.ids.HackHub.dto.requests.team;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ReportViolationRequest(
        @NotBlank(message = "La descrizione della violazione non può essere vuota")
        String description
) {}
