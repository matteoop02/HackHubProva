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
@Table(name = "MENTOR_AVAILABILITY_SLOTS")
public class MentorAvailabilitySlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "MentorId", nullable = false)
    private User mentor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "HackathonId", nullable = false)
    private Hackathon hackathon;

    @Column(name = "StartTime", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "EndTime", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "Notes", length = 500)
    private String notes;

    @Column(name = "Booked", nullable = false)
    private Boolean booked;
}
