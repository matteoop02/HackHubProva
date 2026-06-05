package unicam.ids.HackHub.model;

import jakarta.persistence.*;
import lombok.*;
import unicam.ids.HackHub.enums.SubmissionState;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "SUBMISSIONS")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @Column(name = "Title", nullable = false)
    private String title;

    @Column(name = "Content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "SendingDate", nullable = false)
    private LocalDateTime sendingDate;

    @Column(name = "LastEdit")
    private LocalDateTime lastEdit;

    @Enumerated(EnumType.STRING)
    @Column(name = "State", nullable = false)
    private SubmissionState state;

    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private SubmissionEvaluation evaluation;

    @OneToOne
    @JoinColumn(name = "TeamID")
    private Team team;

    @ManyToOne
    @JoinColumn(name = "HackathonID")
    private Hackathon hackathon;
}
