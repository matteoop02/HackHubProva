package unicam.ids.HackHub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unicam.ids.HackHub.model.SubmissionEvaluation;

import java.util.Optional;

public interface SubmissionEvaluationRepository extends JpaRepository<SubmissionEvaluation, Long> {
    Optional<SubmissionEvaluation> findBySubmissionId(Long submissionId);
}
