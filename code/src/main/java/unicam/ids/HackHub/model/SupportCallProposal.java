package unicam.ids.HackHub.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "SUPPORT_CALL_PROPOSALS")
public class SupportCallProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "MentorId", nullable = false)
    private User mentor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "TeamId", nullable = false)
    private Team team;

    @ManyToOne(optional = false)
    @JoinColumn(name = "HackathonId", nullable = false)
    private Hackathon hackathon;

    @ManyToOne
    @JoinColumn(name = "SlotId")
    private MentorAvailabilitySlot slot;

    @Column(name = "Subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "Message", nullable = false, length = 2000)
    private String message;

    @Column(name = "ProposedStartTime", nullable = false)
    private LocalDateTime proposedStartTime;

    @Column(name = "ProposedEndTime", nullable = false)
    private LocalDateTime proposedEndTime;

    @Column(name = "Status", nullable = false, length = 50)
    private String status;
}
