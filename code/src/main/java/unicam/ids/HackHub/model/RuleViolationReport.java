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
@Table(name = "RULE_VIOLATIONS")
public class RuleViolationReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "TeamId", nullable = false)
    private Team team;

    @ManyToOne
    @JoinColumn(name = "MentorId", nullable = false)
    private User mentor;

    @Column(name = "Description", nullable = false, length = 2000)
    private String description;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;
}
