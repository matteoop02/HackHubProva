package unicam.ids.HackHub.dto.responses;

import lombok.Builder;

import java.util.List;

@Builder
public record SupportedRolesResponse(
        List<String> canonicalRoles,
        List<String> acceptedAliases,
        String defaultRole
) {
}
