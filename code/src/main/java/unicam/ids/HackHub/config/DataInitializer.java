package unicam.ids.HackHub.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import unicam.ids.HackHub.model.UserRole;
import unicam.ids.HackHub.repository.UserRoleRepository;
import unicam.ids.HackHub.util.RoleNames;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRoleRepository userRoleRepository;

    @Override
    public void run(String... args) {
        List.of(
                RoleNames.REGISTERED_USER,
                RoleNames.ORGANIZER,
                RoleNames.JUDGE,
                RoleNames.MENTOR
        ).forEach(this::ensureRoleExists);
    }

    private void ensureRoleExists(String roleName) {
        userRoleRepository.findByNameAndIsActiveTrue(roleName)
                .orElseGet(() -> userRoleRepository.save(UserRole.builder()
                        .name(roleName)
                        .isActive(true)
                        .build()));
    }
}
