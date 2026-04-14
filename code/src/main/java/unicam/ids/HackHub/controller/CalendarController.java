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
import unicam.ids.HackHub.dto.requests.CreateMentorAvailabilitySlotRequest;
import unicam.ids.HackHub.dto.requests.ProposeSupportCallRequest;
import unicam.ids.HackHub.dto.responses.MentorAvailabilitySlotResponse;
import unicam.ids.HackHub.dto.responses.SupportCallProposalResponse;
import unicam.ids.HackHub.service.CalendarService;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@Tag(name = "Calendar", description = "Calendario disponibilita' e call di supporto dei mentori")
public class CalendarController {

    private final CalendarService calendarService;

    @PostMapping("/slots")
    @Operation(summary = "Definisci slot di disponibilita'", description = "Permette a un mentore di dichiarare gli slot disponibili per call di supporto.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Slot Mentore",
                            value = """
                                    {
                                      "hackathonId": 1,
                                      "startTime": "2026-05-10T15:00:00",
                                      "endTime": "2026-05-10T15:30:00",
                                      "notes": "Disponibile per supporto su backend e architettura."
                                    }
                                    """
                    )
            )
    )
    @ApiResponse(responseCode = "201", description = "Slot creato con successo")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida")
    public ResponseEntity<MentorAvailabilitySlotResponse> createAvailabilitySlot(
            Authentication authentication,
            @RequestBody @Valid CreateMentorAvailabilitySlotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(calendarService.createAvailabilitySlot(authentication, request));
    }

    @PostMapping("/support-calls")
    @Operation(summary = "Proponi una call di supporto", description = "Permette a un mentore di proporre una call di supporto a un team.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "Con Slot",
                                    value = """
                                            {
                                              "teamId": 1,
                                              "slotId": 1,
                                              "subject": "Supporto sull'architettura del progetto",
                                              "message": "Possiamo fare una call per rivedere backend e piano di consegna?"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Con Orario Proposto",
                                    value = """
                                            {
                                              "teamId": 1,
                                              "proposedStartTime": "2026-05-10T17:00:00",
                                              "proposedEndTime": "2026-05-10T17:30:00",
                                              "subject": "Debug session",
                                              "message": "Vi propongo una call per analizzare insieme il problema sul deploy."
                                            }
                                            """
                            )
                    }
            )
    )
    @ApiResponse(responseCode = "201", description = "Proposta creata con successo")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida")
    public ResponseEntity<SupportCallProposalResponse> proposeSupportCall(
            Authentication authentication,
            @RequestBody @Valid ProposeSupportCallRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(calendarService.proposeSupportCall(authentication, request));
    }
}
