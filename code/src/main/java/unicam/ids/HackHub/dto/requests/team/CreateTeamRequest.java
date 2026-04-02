package unicam.ids.HackHub.dto.requests.team;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateTeamRequest(
        @NotEmpty(message = "Il nome del team non può essere nullo")
        String name,

        @NotNull(message = "Specificare se il team sarà Pubblico (true) o Privato (false)")
        boolean isPublic,

        Long hackathonId
) {}
