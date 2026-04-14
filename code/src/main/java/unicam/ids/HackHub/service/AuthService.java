package unicam.ids.HackHub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import unicam.ids.HackHub.dto.requests.LoginRequest;
import unicam.ids.HackHub.dto.requests.RegisterRequest;
import unicam.ids.HackHub.dto.responses.AuthResponse;
import unicam.ids.HackHub.dto.responses.SupportedRolesResponse;
import unicam.ids.HackHub.dto.responses.UserResponse;
import unicam.ids.HackHub.exceptions.BusinessLogicException;
import unicam.ids.HackHub.exceptions.ResourceNotFoundException;
import unicam.ids.HackHub.exceptions.UnauthorizedAccessException;
import unicam.ids.HackHub.model.User;
import unicam.ids.HackHub.model.UserRole;
import unicam.ids.HackHub.repository.UserRepository;
import unicam.ids.HackHub.repository.UserRoleRepository;
import unicam.ids.HackHub.security.JwtService;
import unicam.ids.HackHub.util.RoleNames;

import java.sql.Date;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Map<String, String> ROLE_ALIASES = buildRoleAliases();

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessLogicException("Username gia' in uso");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessLogicException("Email gia' in uso");
        }

        UserRole role = resolveRole(request.roleName());

        User user = User.builder()
                .name(request.name())
                .surname(request.surname())
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .dateOfBirth(request.dateOfBirth() != null ? Date.valueOf(request.dateOfBirth()) : null)
                .role(role)
                .isDeleted(false)
                .build();

        User savedUser = userRepository.save(user);
        return buildAuthResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameAndIsDeletedFalse(request.username())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedAccessException("Credenziali non valide");
        }

        return buildAuthResponse(user);
    }

    public SupportedRolesResponse getSupportedRoles() {
        List<String> canonicalRoles = userRoleRepository.findAll().stream()
                .filter(UserRole::getIsActive)
                .map(UserRole::getName)
                .sorted()
                .collect(Collectors.toList());

        return SupportedRolesResponse.builder()
                .canonicalRoles(canonicalRoles)
                .acceptedAliases(new ArrayList<>(ROLE_ALIASES.keySet()))
                .defaultRole(RoleNames.REGISTERED_USER)
                .build();
    }

    public UserResponse getAuthenticatedUser(org.springframework.security.core.Authentication authentication) {
        User user = userRepository.findByUsernameAndIsDeletedFalse(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));
        return mapToUserResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        return AuthResponse.builder()
                .token(jwtService.generateToken(user.getUsername(), user.getRole().getName()))
                .user(mapToUserResponse(user))
                .actors(resolveActors(user))
                .build();
    }

    private UserRole resolveRole(String roleName) {
        String normalizedRole = roleName == null || roleName.isBlank()
                ? RoleNames.REGISTERED_USER
                : roleName.trim().toUpperCase(Locale.ROOT);

        String resolvedRole = ROLE_ALIASES.getOrDefault(normalizedRole, normalizedRole);

        return userRoleRepository.findByNameAndIsActiveTrue(resolvedRole)
                .orElseThrow(() -> new ResourceNotFoundException("Ruolo non supportato: " + resolvedRole));
    }

    private static Map<String, String> buildRoleAliases() {
        Map<String, String> aliases = new LinkedHashMap<>();
        aliases.put("REGISTERED_USER", RoleNames.REGISTERED_USER);
        aliases.put("REGISTERED", RoleNames.REGISTERED_USER);
        aliases.put("USER", RoleNames.REGISTERED_USER);
        aliases.put("ORGANIZER", RoleNames.ORGANIZER);
        aliases.put("ORGANIZZATORE", RoleNames.ORGANIZER);
        aliases.put("JUDGE", RoleNames.JUDGE);
        aliases.put("GIUDICE", RoleNames.JUDGE);
        aliases.put("MENTOR", RoleNames.MENTOR);
        return aliases;
    }

    private List<String> resolveActors(User user) {
        List<String> actors = new ArrayList<>();
        actors.add("UTENTE_REGISTRATO");

        if (user.getTeam() != null) {
            actors.add("MEMBRO_DEL_TEAM");
            if (user.getTeam().getTeamLeader() != null && user.getTeam().getTeamLeader().getId().equals(user.getId())) {
                actors.add("LEADER_DEL_TEAM");
            }
        }

        if (RoleNames.ORGANIZER.equals(user.getRole().getName())) {
            actors.add("ORGANIZZATORE_HACKATHON");
        }
        if (RoleNames.JUDGE.equals(user.getRole().getName())) {
            actors.add("GIUDICE_HACKATHON");
        }
        if (RoleNames.MENTOR.equals(user.getRole().getName())) {
            actors.add("MENTORE_HACKATHON");
        }

        return actors;
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .username(user.getUsername())
                .email(user.getEmail())
                .roleName(user.getRole().getName())
                .build();
    }
}
