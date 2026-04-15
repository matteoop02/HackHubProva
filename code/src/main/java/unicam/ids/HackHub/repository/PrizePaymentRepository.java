package unicam.ids.HackHub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unicam.ids.HackHub.model.PrizePayment;

import java.util.Optional;

public interface PrizePaymentRepository extends JpaRepository<PrizePayment, Long> {
    Optional<PrizePayment> findByHackathonId(Long hackathonId);
}
