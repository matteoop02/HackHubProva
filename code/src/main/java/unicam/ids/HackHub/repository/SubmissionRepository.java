package unicam.ids.HackHub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unicam.ids.HackHub.enums.SubmissionState;
import unicam.ids.HackHub.model.Submission;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    
    List<Submission> findByHackathonJudgeUsername(String username);
    
    List<Submission> findByTeamMentorsUsername(String username);
}
