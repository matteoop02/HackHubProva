package unicam.ids.HackHub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import unicam.ids.HackHub.dto.requests.AcceptInsideInviteRequest;
import unicam.ids.HackHub.dto.requests.invite.*;
import unicam.ids.HackHub.dto.responses.InviteResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import unicam.ids.HackHub.service.InviteService;

@RestController
@RequestMapping("/api/invites")
@RequiredArgsConstructor
@Tag(name = "Inviti", description = "Gestione degli inviti: Interni (da e verso i team) ed Esterni (per utenti invitati ad iscriversi alla piattaforma)")
public class InviteController {

    private final InviteService inviteService;

    @PostMapping("/outside/rejectOutsideInvite")
    @Operation(summary = "Rifiuta invito esterno.", description = "Permette a un utente di rifiutare l’invito esterno.", requestBody = @RequestBody(required = true, content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Esempio di rifiuto.", value = """
            {
              "token": "abc123TOKENabc123",
            }
            """))))
    @ApiResponse(responseCode = "200", description = "Rifiuto avvenuto con successo")
    @ApiResponse(responseCode = "400", description = "Errore nella richiesta o dati non validi")
    public ResponseEntity<String> rejectOutsideInvite(
            @RequestBody @Valid RejectOutsideInviteRequest rejectOutsideInviteRequest) {
        inviteService.rejectOutsideInvite(rejectOutsideInviteRequest);
        return ResponseEntity.ok("Rifiuto dell'invito riuscito!");
    }

    @PostMapping("/leaderDelTeam/inviteToTeam")
    @Operation(summary = "Invita un utente al team", description = "Permette al leader di un team di invitare un utente già registrato a unirsi al team.")
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Invito Interno",
                            value = """
                                    {
                                      "recipientId": 2,
                                      "proposedRoleId": 1,
                                      "message": "Ti invitiamo a unirti al nostro team per l'hackathon."
                                    }
                                    """
                    )
            )
    )
    @ApiResponse(responseCode = "201", description = "Invito al team creato con successo")
    @ApiResponse(responseCode = "400", description = "Errore nella richiesta o utente non valido")
    public ResponseEntity<InviteResponse> inviteToTeam(
            Authentication authentication,
            @RequestBody @Valid InsideInviteRequest insideInviteRequest) {

        InviteResponse invite = inviteService.inviteUserToTeam(authentication, insideInviteRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(invite);
    }

    @PostMapping("/inviteManage/acceptTeamInvite")
    @Operation(
            summary = "Accetta invito al team",
            description = "Permette a un utente autenticato di accettare un invito ricevuto per entrare in un team.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Accettazione Invito Team",
                                    value = """
                                            {
                                              "inviteId": 1
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponse(responseCode = "200", description = "Invito accettato con successo")
    @ApiResponse(responseCode = "400", description = "Errore durante l'accettazione dell'invito")
    public ResponseEntity<InviteResponse> acceptTeamInvite(Authentication authentication,
            @RequestBody @Valid AcceptInsideInviteRequest request) {
        return ResponseEntity.ok(inviteService.acceptTeamInvite(authentication, request));
    }

    @PostMapping("/inviteManage/rejectTeamInvite")
    @Operation(summary = "Rifiuta invito al team", description = "Permette a un utente autenticato di rifiutare un invito ricevuto per entrare in un team.")
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Rifiuto Invito Team",
                            value = """
                                    {
                                      "inviteId": 1
                                    }
                                    """
                    )
            )
    )
    @ApiResponse(responseCode = "200", description = "Invito rifiutato con successo")
    @ApiResponse(responseCode = "400", description = "Errore durante il rifiuto dell'invito")
    public ResponseEntity<String> rejectTeamInvite(Authentication authentication, 
            @RequestBody @Valid RejectInsideInviteRequest rejectInsideInviteRequest) {
        inviteService.rejectTeamInvite(authentication, rejectInsideInviteRequest);
        return ResponseEntity.ok("Invito rifiutato con successo!");
    }

    @PostMapping("/outside/create")
    @Operation(summary = "Invita utente esterno", description = "Invia un invito via email a un utente esterno alla piattaforma.")
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Invito Esterno",
                            value = """
                                    {
                                      "recipientEmail": "nuovo.utente@example.com",
                                      "message": "Vorremmo invitarti a registrarti su HackHub e collaborare con noi."
                                    }
                                    """
                    )
            )
    )
    @ApiResponse(responseCode = "200", description = "Invito creato e mail inviata")
    @ApiResponse(responseCode = "400", description = "Errore nella richiesta")
    public ResponseEntity<String> createOutsideInvite(Authentication authentication, 
            @RequestBody @Valid CreateOutsideInviteRequest request) {
        inviteService.createOutsideInvite(authentication, request);
        return ResponseEntity.ok("Invito inviato con successo!");
    }
}
