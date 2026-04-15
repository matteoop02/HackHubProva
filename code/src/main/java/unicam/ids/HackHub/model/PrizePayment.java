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
@Table(name = "PRIZE_PAYMENTS")
public class PrizePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "HackathonId", nullable = false, unique = true)
    private Hackathon hackathon;

    @ManyToOne(optional = false)
    @JoinColumn(name = "TeamId", nullable = false)
    private Team team;

    @Column(name = "Amount", nullable = false)
    private Double amount;

    @Column(name = "PaidAt", nullable = false)
    private LocalDateTime paidAt;

    @Column(name = "TransactionReference", nullable = false, unique = true, length = 100)
    private String transactionReference;

    @Column(name = "Status", nullable = false, length = 50)
    private String status;
}
