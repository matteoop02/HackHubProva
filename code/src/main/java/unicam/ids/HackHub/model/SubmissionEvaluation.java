package unicam.ids.HackHub.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "SUBMISSION_EVALUATIONS")
public class SubmissionEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "SubmissionId", nullable = false, unique = true)
    private Submission submission;

    @ManyToOne(optional = false)
    @JoinColumn(name = "JudgeId", nullable = false)
    private User judge;

    @DecimalMin("0.0")
    @DecimalMax("10.0")
    @Column(name = "Score", nullable = false)
    private Double score;

    @Column(name = "Comment", length = 1000)
    private String comment;

    @Column(name = "EvaluatedAt", nullable = false)
    private LocalDateTime evaluatedAt;

    @Column(name = "LastModifiedAt")
    private LocalDateTime lastModifiedAt;
}
