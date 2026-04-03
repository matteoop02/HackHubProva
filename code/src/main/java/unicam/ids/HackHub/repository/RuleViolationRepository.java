package unicam.ids.HackHub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unicam.ids.HackHub.model.RuleViolationReport;

public interface RuleViolationRepository extends JpaRepository<RuleViolationReport, Long> {
}
