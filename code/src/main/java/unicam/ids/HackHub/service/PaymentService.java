package unicam.ids.HackHub.service;

import org.springframework.stereotype.Service;
import unicam.ids.HackHub.exceptions.BusinessLogicException;
import unicam.ids.HackHub.model.Hackathon;
import unicam.ids.HackHub.model.PrizePayment;
import unicam.ids.HackHub.model.Team;
import unicam.ids.HackHub.repository.PrizePaymentRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private final PrizePaymentRepository prizePaymentRepository;
    private final EmailService emailService;
    private final TeamMembershipService teamMembershipService;

    public PaymentService(PrizePaymentRepository prizePaymentRepository, EmailService emailService,
            TeamMembershipService teamMembershipService) {
        this.prizePaymentRepository = prizePaymentRepository;
        this.emailService = emailService;
        this.teamMembershipService = teamMembershipService;
    }

    public PrizePayment payWinningTeam(Hackathon hackathon) {
        if (hackathon.getTeamWinner() == null) {
            throw new BusinessLogicException("Non e' possibile erogare il premio senza un team vincitore");
        }

        return prizePaymentRepository.findByHackathonId(hackathon.getId())
                .orElseGet(() -> createPayment(hackathon));
    }

    private PrizePayment createPayment(Hackathon hackathon) {
        Team winningTeam = hackathon.getTeamWinner();
        PrizePayment payment = PrizePayment.builder()
                .hackathon(hackathon)
                .team(winningTeam)
                .amount(hackathon.getReward())
                .paidAt(LocalDateTime.now())
                .transactionReference("PAY-" + UUID.randomUUID())
                .status("EROGATO")
                .build();

        PrizePayment savedPayment = prizePaymentRepository.save(payment);

        teamMembershipService.getMembers(winningTeam).forEach(member -> emailService.sendEmail(
                member.getEmail(),
                "Premio erogato per l'hackathon " + hackathon.getName(),
                "Il sistema di pagamento ha erogato il premio di " + hackathon.getReward()
                        + " euro al team '" + winningTeam.getName()
                        + "'. Riferimento transazione: " + savedPayment.getTransactionReference()
        ));

        return savedPayment;
    }
}
