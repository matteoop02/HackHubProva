package unicam.ids.HackHub.service;

import org.springframework.stereotype.Service;
import unicam.ids.HackHub.dto.requests.submission.UpdateTeamSubmissionRequest;
import unicam.ids.HackHub.model.Submission;
import unicam.ids.HackHub.model.User;
import unicam.ids.HackHub.repository.SubmissionRepository;
import unicam.ids.HackHub.repository.UserRepository;
import unicam.ids.HackHub.exceptions.ResourceNotFoundException;
import unicam.ids.HackHub.exceptions.UnauthorizedAccessException;

import java.time.LocalDateTime;
import java.util.List;

import unicam.ids.HackHub.dto.responses.SubmissionResponse;
import java.util.stream.Collectors;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    public SubmissionService(SubmissionRepository submissionRepository, UserRepository userRepository) {
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
    }

    public List<SubmissionResponse> getSubmissionsByStaffMember(String username) {
        User user = userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));

        List<Submission> submissions;
        if ("GIUDICE".equals(user.getRole().getName())) {
            submissions = submissionRepository.findByHackathonJudgeUsername(username);
        } else if ("MENTOR".equals(user.getRole().getName())) {
            submissions = submissionRepository.findByTeamMentorsUsername(username);
        } else {
            throw new UnauthorizedAccessException("L'utente non e' autorizzato (deve essere GIUDICE o MENTOR)");
        }

        return submissions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void updateSubmission(UpdateTeamSubmissionRequest request) {
        Submission submission = submissionRepository.findById(request.submissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Sottomissione non trovata"));

        submission.setContent(request.content());
        submission.setTitle(request.title());
        submission.setLastEdit(LocalDateTime.now());
        submissionRepository.save(submission);
    }

    private SubmissionResponse mapToResponse(Submission submission) {
        return SubmissionResponse.builder()
                .id(submission.getId())
                .title(submission.getTitle())
                .content(submission.getContent())
                .submittedAt(submission.getSendingDate())
                .teamId(submission.getTeam() != null ? submission.getTeam().getId() : null)
                .teamName(submission.getTeam() != null ? submission.getTeam().getName() : "N/A")
                .build();
    }
}
