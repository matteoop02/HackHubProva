package unicam.ids.HackHub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import unicam.ids.HackHub.dto.requests.submission.*;
import unicam.ids.HackHub.dto.responses.SubmissionResponse;
import unicam.ids.HackHub.service.SubmissionService;
import java.util.List;

@RestController
@RequestMapping("/api/submission")
@RequiredArgsConstructor
@Tag(name = "Sottomissioni", description = "Gestione delle sottomissioni")
public class SubmissionController {

    private final SubmissionService submissionService;

    // ------------------------------- STAFF -------------------------------

    /**
     * Recupera tutte le sottomissioni assegnate allo staff autenticato.
     */
    @GetMapping("/staff/listByStaffMember")
    @Operation(summary = "Lista sottomissioni per membro dello staff", description = "Recupera tutte le sottomissioni associate allo staff autenticato.")
    @ApiResponse(responseCode = "200", description = "Sottomissioni recuperate con successo")
    @ApiResponse(responseCode = "400", description = "Errore nella richiesta")
    public ResponseEntity<List<SubmissionResponse>> getSubmissionsListByHackathonName(Authentication authentication) {
        return ResponseEntity.ok(submissionService.getSubmissionsByStaffMember(authentication.getName()));
    }

    /**
     * Aggiorna una sottomissione esistente.
     */
    @PostMapping("/team/update")
    @Operation(summary = "Aggiorna sottomissione team", description = "Permette di aggiornare una sottomissione già esistente per il team.")
    @ApiResponse(responseCode = "200", description = "Sottomissione aggiornata con successo")
    @ApiResponse(responseCode = "400", description = "Errore nella richiesta")
    public ResponseEntity<String> updateSubmission(
            @Valid @RequestBody UpdateTeamSubmissionRequest updateTeamSubmissionRequest) {
        submissionService.updateSubmission(updateTeamSubmissionRequest);
        return ResponseEntity.ok("Sottomissione aggiornata con successo");
    }

    @PostMapping("/staff/evaluate")
    @Operation(summary = "Valuta sottomissione (Giudice)", description = "Permette al giudice dell'hackathon di valutare una sottomissione assegnando score e commento.")
    @ApiResponse(responseCode = "200", description = "Sottomissione valutata con successo")
    @ApiResponse(responseCode = "400", description = "Errore nella valutazione")
    public ResponseEntity<String> evaluateSubmission(Authentication authentication, 
            @Valid @RequestBody EvaluateSubmissionRequest request) {
        submissionService.evaluateSubmission(authentication, request);
        return ResponseEntity.ok("Sottomissione valutata con successo");
    }
}