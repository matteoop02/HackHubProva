package unicam.ids.HackHub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import unicam.ids.HackHub.dto.requests.AssignJudgeRequest;
import unicam.ids.HackHub.dto.requests.AssignMentorsRequest;
import unicam.ids.HackHub.dto.requests.DeclareWinningTeamRequest;
import unicam.ids.HackHub.dto.requests.hackathon.CreateHackathonRequest;
import unicam.ids.HackHub.dto.responses.HackathonResponse;
import unicam.ids.HackHub.dto.responses.PrizePaymentStatusResponse;
import unicam.ids.HackHub.service.HackathonManagementService;

import java.util.List;

@RestController
@RequestMapping("/api/hackathon")
@RequiredArgsConstructor
@Tag(name = "Hackathon", description = "Gestione degli hackathon")
public class HackathonController {

    private final HackathonManagementService hackathonService;

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

    @GetMapping("/{id}")
    @Operation(summary = "Dettaglio hackathon", description = "Restituisce le informazioni di un hackathon. Senza autenticazione sono visibili solo gli hackathon pubblici.")
    @ApiResponse(responseCode = "200", description = "Hackathon ottenuto con successo")
    @ApiResponse(responseCode = "404", description = "Hackathon non trovato")
    public ResponseEntity<HackathonResponse> getHackathonById(Authentication authentication, @PathVariable Long id) {
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated();
        return ResponseEntity.ok(hackathonService.getHackathonById(id, isAuthenticated));
    }

    @PostMapping("/organizzatore/create")
    @Operation(summary = "Creazione nuovo hackathon", description = "Permette la registrazione di un nuovo hackathon")
    @ApiResponse(responseCode = "200", description = "Hackathon registrato con successo")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida o dati mancanti")
    public ResponseEntity<String> createHackathon(Authentication authentication,
            @RequestBody @Valid CreateHackathonRequest createHackathonRequest) {
        hackathonService.createHackathon(authentication, createHackathonRequest);
        return ResponseEntity.ok("Hackathon creato con successo");
    }

    @PostMapping("/{id}/start")
    @Hidden
    @Operation(summary = "Avvia Hackathon", description = "Permette all'organizzatore di avviare l'hackathon (passando allo stato IN_CORSO)")
    @ApiResponse(responseCode = "200", description = "Hackathon avviato")
    @ApiResponse(responseCode = "400", description = "Errore nell'avvio")
    public ResponseEntity<String> startHackathon(Authentication authentication, @PathVariable Long id) {
        hackathonService.startHackathon(authentication, id);
        return ResponseEntity.ok("Hackathon avviato con successo");
    }

    @PostMapping("/{id}/closeSubscriptions")
    @Hidden
    @Operation(summary = "Termina iscrizioni Hackathon", description = "Permette all'organizzatore di terminare le iscrizioni (passando allo stato IN_CORSO)")
    @ApiResponse(responseCode = "200", description = "Iscrizioni terminate")
    @ApiResponse(responseCode = "400", description = "Errore nella chiusura")
    public ResponseEntity<String> closeSubscriptions(Authentication authentication, @PathVariable Long id) {
        hackathonService.closeHackathonSubscriptions(authentication, id);
        return ResponseEntity.ok("Iscrizioni terminate con successo");
    }

    @PostMapping("/{id}/winner")
    @Operation(summary = "Proclama team vincitore", description = "Permette all'organizzatore di proclamare il team vincitore.")
    @ApiResponse(responseCode = "200", description = "Team vincitore proclamato con successo")
    @ApiResponse(responseCode = "400", description = "Errore nella proclamazione")
    public ResponseEntity<HackathonResponse> declareWinningTeam(Authentication authentication,
            @PathVariable Long id,
            @RequestBody @Valid DeclareWinningTeamRequest request) {
        return ResponseEntity.ok(hackathonService.declareWinningTeam(authentication, id, request));
    }

    @PostMapping("/{id}/pay-prize")
    @Operation(summary = "Eroga premio al team vincitore", description = "Permette all'organizzatore di avviare separatamente l'erogazione del premio al team vincitore.")
    @ApiResponse(responseCode = "200", description = "Premio erogato con successo")
    @ApiResponse(responseCode = "400", description = "Errore durante l'erogazione del premio")
    public ResponseEntity<String> payPrize(Authentication authentication, @PathVariable Long id) {
        hackathonService.payPrize(authentication, id);
        return ResponseEntity.ok("Premio erogato con successo");
    }

    @GetMapping("/{id}/payment-status")
    @Operation(summary = "Verifica stato pagamento premio", description = "Permette all'organizzatore di verificare lo stato di pagamento del premio tramite il sistema di pagamento.")
    @ApiResponse(responseCode = "200", description = "Stato pagamento recuperato con successo")
    public ResponseEntity<PrizePaymentStatusResponse> getPaymentStatus(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(hackathonService.getPaymentStatus(authentication, id));
    }

    @PostMapping("/{id}/judge")
    @Operation(summary = "Aggiungi giudice all'hackathon", description = "Permette all'organizzatore di assegnare un giudice all'hackathon.")
    public ResponseEntity<String> assignJudge(Authentication authentication, @PathVariable Long id,
            @RequestBody @Valid AssignJudgeRequest request) {
        hackathonService.assignJudge(authentication, id, request);
        return ResponseEntity.ok("Giudice assegnato con successo");
    }

    @PostMapping("/{id}/mentors")
    @Operation(summary = "Aggiungi mentori all'hackathon", description = "Permette all'organizzatore di assegnare uno o piu' mentori all'hackathon.")
    public ResponseEntity<String> assignMentors(Authentication authentication, @PathVariable Long id,
            @RequestBody @Valid AssignMentorsRequest request) {
        hackathonService.assignMentors(authentication, id, request);
        return ResponseEntity.ok("Mentori assegnati con successo");
    }

    @DeleteMapping("/{id}/mentors/{mentorId}")
    @Operation(summary = "Rimuovi mentore dall'hackathon", description = "Permette all'organizzatore di rimuovere un mentore dall'hackathon.")
    public ResponseEntity<String> removeMentor(Authentication authentication, @PathVariable Long id,
            @PathVariable Long mentorId) {
        hackathonService.removeMentor(authentication, id, mentorId);
        return ResponseEntity.ok("Mentore rimosso con successo");
    }

}
