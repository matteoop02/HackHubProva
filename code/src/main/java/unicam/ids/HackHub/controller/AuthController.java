package unicam.ids.HackHub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import unicam.ids.HackHub.dto.requests.LoginRequest;
import unicam.ids.HackHub.dto.requests.RegisterRequest;
import unicam.ids.HackHub.dto.responses.AuthResponse;
import unicam.ids.HackHub.dto.responses.SupportedRolesResponse;
import unicam.ids.HackHub.dto.responses.UserResponse;
import unicam.ids.HackHub.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticazione", description = "Registrazione e login degli utenti della piattaforma")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = "Registrazione",
            description = "Registra un nuovo utente sulla piattaforma. Il token JWT restituito puo' essere usato subito in Swagger tramite il pulsante Authorize.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Organizzatore",
                                            value = """
                                                    {
                                                      "name": "Mario",
                                                      "surname": "Rossi",
                                                      "username": "mario.rossi",
                                                      "email": "mario.rossi@example.com",
                                                      "password": "Password123!",
                                                      "dateOfBirth": "1999-05-14",
                                                      "roleName": "ORGANIZER"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Utente Default",
                                            value = """
                                                    {
                                                      "name": "Luca",
                                                      "surname": "Bianchi",
                                                      "username": "luca.bianchi",
                                                      "email": "luca.bianchi@example.com",
                                                      "password": "Password123!",
                                                      "dateOfBirth": "2000-03-21"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    )
    @ApiResponse(responseCode = "201", description = "Utente registrato con successo")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login",
            description = "Autentica un utente registrato e restituisce un token JWT. Copia il campo token e usalo nel pulsante Authorize di Swagger.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Login Base",
                                    value = """
                                            {
                                              "username": "mario.rossi",
                                              "password": "Password123!"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponse(responseCode = "200", description = "Login eseguito con successo")
    @ApiResponse(responseCode = "403", description = "Credenziali non valide")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/supported-roles")
    @Operation(summary = "Ruoli supportati", description = "Restituisce i ruoli canonici e gli alias accettati durante la registrazione.")
    @ApiResponse(responseCode = "200", description = "Ruoli ottenuti con successo")
    public ResponseEntity<SupportedRolesResponse> getSupportedRoles() {
        return ResponseEntity.ok(authService.getSupportedRoles());
    }

    @GetMapping("/me")
    @Operation(summary = "Profilo autenticato", description = "Restituisce i dati dell'utente autenticato. Utile per verificare velocemente che il token inserito in Swagger sia valido.")
    @ApiResponse(responseCode = "200", description = "Utente autenticato ottenuto con successo")
    @ApiResponse(responseCode = "403", description = "Token mancante o non valido")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        return ResponseEntity.ok(authService.getAuthenticatedUser(authentication));
    }
}
