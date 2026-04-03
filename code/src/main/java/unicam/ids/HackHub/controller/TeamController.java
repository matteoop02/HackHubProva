package unicam.ids.HackHub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import unicam.ids.HackHub.dto.requests.team.CreateTeamRequest;
import unicam.ids.HackHub.dto.responses.TeamResponse;
import unicam.ids.HackHub.service.TeamService;

@RestController
@RequestMapping("/api/team")
@RequiredArgsConstructor
@Tag(name = "Team", description = "Gestione dinamiche dei team")
public class TeamController {
    private final TeamService teamService;

    @PostMapping("/utente/create")
    @Operation(summary = "Creazione nuovo team", description = "Permette la registrazione di un nuovo team da parte di un Utente")
    @ApiResponse(responseCode = "200", description = "Team registrato con successo")
    @ApiResponse(responseCode = "400", description = "Team non creato,errore")
    public ResponseEntity<TeamResponse> createTeam(Authentication authentication,
            @RequestBody @Valid CreateTeamRequest createTeamRequest) {
        TeamResponse team = teamService.createTeam(authentication, createTeamRequest);
        return ResponseEntity.ok(team);
    }

    @DeleteMapping("/members/me")
    @Operation(summary = "Lascia il team", description = "Permette a un utente di lasciare il proprio team.")
    @ApiResponse(responseCode = "200", description = "Team lasciato con successo")
    @ApiResponse(responseCode = "400", description = "Errore nella richiesta")
    public ResponseEntity<String> leaveTeam(Authentication authentication) {
        teamService.leaveTeam(authentication);
        return ResponseEntity.ok("Hai lasciato il team con successo");
    }

    @PostMapping("/{teamId}/subscribe/{hackathonId}")
    @Operation(summary = "Iscrizione team a Hackathon", description = "Permette al leader di un team di iscrivere il suo team a un hackathon")
    @ApiResponse(responseCode = "200", description = "Team iscritto con successo")
    @ApiResponse(responseCode = "400", description = "Errore durante l'iscrizione")
    public ResponseEntity<String> subscribeTeam(Authentication authentication, @PathVariable Long teamId, @PathVariable Long hackathonId) {
        teamService.subscribeTeamToHackathon(authentication, teamId, hackathonId);
        return ResponseEntity.ok("Team iscritto con successo all'hackathon");
    }

    @PostMapping("/{teamId}/violation/report")
    @Operation(summary = "Segnala Violazione Team", description = "Permette a un mentore di segnalare una violazione da parte del team all'organizzatore.")
    @ApiResponse(responseCode = "200", description = "Segnalazione inviata con successo")
    @ApiResponse(responseCode = "400", description = "Errore nella segnalazione")
    public ResponseEntity<String> reportViolation(Authentication authentication, 
            @PathVariable Long teamId, 
            @Valid @RequestBody unicam.ids.HackHub.dto.requests.team.ReportViolationRequest request) {
        teamService.reportViolation(authentication, teamId, request);
        return ResponseEntity.ok("Violazione segnalata con successo all'organizzatore");
    }
}
