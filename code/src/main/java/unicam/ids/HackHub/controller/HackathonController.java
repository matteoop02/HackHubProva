package unicam.ids.HackHub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import unicam.ids.HackHub.dto.requests.hackathon.CreateHackathonRequest;
import unicam.ids.HackHub.dto.responses.HackathonResponse;
import unicam.ids.HackHub.service.HackathonService;

import java.util.List;

@RestController
@RequestMapping("/api/hackathon")
@RequiredArgsConstructor
@Tag(name = "Hackathon", description = "Gestione degli hackathon")
public class HackathonController {

    private final HackathonService hackathonService;

    // --------------------------------- GET VARI ---------------------------------

    @GetMapping("/public")
    @Operation(summary = "Consultazione Hackathon", description = """
                Restituisce la lista degli hackathon.
                Se l'utente è autenticato → ritorna tutti gli hackathon (pubblici + privati).
                Se l'utente non è autenticato → ritorna solo gli hackathon pubblici.
            """)
    @ApiResponse(responseCode = "200", description = "Lista hackathon ottenuta con successo")
    @ApiResponse(responseCode = "400", description = "Errore nella richiesta o dati non validi")
    public ResponseEntity<List<HackathonResponse>> getHackathonsList(Authentication authentication) {
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated();
        return ResponseEntity.ok(hackathonService.getHackathons(isAuthenticated));
    }

    @PostMapping("/organizzatore/create")
    @Operation(summary = "Creazione nuovo hackathon", description = "Permette la registrazione di un nuovo hackathon", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dati dell'hackathon da registrare", required = true, content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Esempio registrazione", value = """
                {
                  "name": "Hackathon Innovazione 2026",
                  "place": "Camerino",
                  "regulation": "Testo...",
                  "subscriptionDeadline": "2026-04-01T23:59:59",
                  "startDate": "2026-05-01T23:59:59",
                  "endDate": "2026-05-03T23:59:59",
                  "reward": 5000.0,
                  "maxTeamSize": 5,
                  "isPublic": true
                }
            """))))
    @ApiResponse(responseCode = "200", description = "Hackathon registrato con successo")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida o dati mancanti")
    public ResponseEntity<String> createHackathon(Authentication authentication,
            @RequestBody @Valid CreateHackathonRequest createHackathonRequest) {
        hackathonService.createHackathon(authentication, createHackathonRequest);
        return ResponseEntity.ok("Hackathon creato con successo");
    }
}