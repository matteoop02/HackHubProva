package unicam.ids.HackHub.service;

import org.springframework.stereotype.Service;
import unicam.ids.HackHub.dto.requests.SendSubmissionRequest;
import unicam.ids.HackHub.dto.requests.submission.UpdateTeamSubmissionRequest;
import unicam.ids.HackHub.enums.HackathonRole;
import unicam.ids.HackHub.exceptions.BusinessLogicException;
import unicam.ids.HackHub.exceptions.ResourceNotFoundException;
import unicam.ids.HackHub.exceptions.UnauthorizedAccessException;
import unicam.ids.HackHub.model.Submission;
import unicam.ids.HackHub.model.SubmissionEvaluation;
import unicam.ids.HackHub.model.Team;
import unicam.ids.HackHub.model.User;
import unicam.ids.HackHub.repository.SubmissionEvaluationRepository;
import unicam.ids.HackHub.repository.SubmissionRepository;
import unicam.ids.HackHub.repository.TeamRepository;
import unicam.ids.HackHub.repository.UserRepository;
import unicam.ids.HackHub.util.RoleNames;

import java.time.LocalDateTime;
import java.util.List;

import unicam.ids.HackHub.dto.responses.SubmissionResponse;
import java.util.stream.Collectors;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final SubmissionEvaluationRepository submissionEvaluationRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMembershipService teamMembershipService;
    private final HackathonRoleAssignmentService hackathonRoleAssignmentService;

    public SubmissionService(SubmissionRepository submissionRepository,
            SubmissionEvaluationRepository submissionEvaluationRepository, UserRepository userRepository,
            TeamRepository teamRepository, TeamMembershipService teamMembershipService,
            HackathonRoleAssignmentService hackathonRoleAssignmentService) {
        this.submissionRepository = submissionRepository;
        this.submissionEvaluationRepository = submissionEvaluationRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.teamMembershipService = teamMembershipService;
        this.hackathonRoleAssignmentService = hackathonRoleAssignmentService;
    }

    public List<SubmissionResponse> getSubmissionsByStaffMember(String username) {
        User user = userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));

        List<Submission> submissions;
        if (RoleNames.JUDGE.equals(user.getRole().getName())
                || hackathonRoleAssignmentService.hasAnyAssignment(user, HackathonRole.JUDGE)) {
            submissions = submissionRepository.findByHackathonJudgeUsername(username);
        } else if (RoleNames.MENTOR.equals(user.getRole().getName())
                || hackathonRoleAssignmentService.hasAnyAssignment(user, HackathonRole.MENTOR)) {
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

    public SubmissionResponse sendSubmission(org.springframework.security.core.Authentication authentication,
            SendSubmissionRequest request) {
        User sender = userRepository.findByUsernameAndIsDeletedFalse(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));

        Team team = teamRepository.findById(request.teamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team non trovato"));

        if (!teamMembershipService.isLeader(sender, team)) {
            throw new UnauthorizedAccessException("Solo il leader del team puo' inviare la sottomissione");
        }
        if (team.getHackathon() == null) {
            throw new BusinessLogicException("Il team non e' iscritto a nessun hackathon");
        }
        if (LocalDateTime.now().isAfter(team.getHackathon().getEndDate())) {
            throw new BusinessLogicException("La scadenza per inviare la sottomissione e' gia' trascorsa");
        }

        Submission submission = submissionRepository.findByTeamId(team.getId())
                .orElse(Submission.builder()
                        .team(team)
                        .hackathon(team.getHackathon())
                        .build());

        submission.setTitle(request.title());
        submission.setContent(request.content());
        submission.setSendingDate(LocalDateTime.now());
        submission.setLastEdit(LocalDateTime.now());
        submission.setState(unicam.ids.HackHub.enums.SubmissionState.INVIATA);

        return mapToResponse(submissionRepository.save(submission));
    }

    public void evaluateSubmission(org.springframework.security.core.Authentication authentication, unicam.ids.HackHub.dto.requests.submission.EvaluateSubmissionRequest request) {
        User evaluator = userRepository.findByUsernameAndIsDeletedFalse(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));

        Submission submission = submissionRepository.findById(request.submissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Sottomissione non trovata"));

        if (!RoleNames.JUDGE.equals(evaluator.getRole().getName())
                && (submission.getHackathon() == null
                || !hackathonRoleAssignmentService.hasRole(evaluator, submission.getHackathon(), HackathonRole.JUDGE))) {
            throw new UnauthorizedAccessException("Solo i giudici possono valutare le sottomissioni");
        }

        if (submission.getHackathon() == null
                || !hackathonRoleAssignmentService.hasRole(evaluator, submission.getHackathon(), HackathonRole.JUDGE)) {
            throw new UnauthorizedAccessException("Non sei il giudice di questo hackathon");
        }
        if (submission.getHackathon().getState() != unicam.ids.HackHub.enums.HackathonState.IN_VALUTAZIONE) {
            throw new BusinessLogicException("La valutazione puo' essere inserita o modificata solo durante la fase di valutazione");
        }

        LocalDateTime now = LocalDateTime.now();
        SubmissionEvaluation evaluation = submissionEvaluationRepository.findBySubmissionId(submission.getId())
                .orElse(SubmissionEvaluation.builder()
                        .submission(submission)
                        .judge(evaluator)
                        .evaluatedAt(now)
                        .build());

        evaluation.setJudge(evaluator);
        evaluation.setScore(request.score());
        evaluation.setComment(request.comment());
        evaluation.setLastModifiedAt(now);

        submission.setState(unicam.ids.HackHub.enums.SubmissionState.VALUTATA);

        SubmissionEvaluation savedEvaluation = submissionEvaluationRepository.save(evaluation);
        submission.setEvaluation(savedEvaluation);
        submissionRepository.save(submission);
    }

    private SubmissionResponse mapToResponse(Submission submission) {
        SubmissionEvaluation evaluation = submission.getEvaluation();
        User judge = evaluation != null ? evaluation.getJudge() : null;

        return SubmissionResponse.builder()
                .id(submission.getId())
                .title(submission.getTitle())
                .content(submission.getContent())
                .submittedAt(submission.getSendingDate())
                .teamId(submission.getTeam() != null ? submission.getTeam().getId() : null)
                .teamName(submission.getTeam() != null ? submission.getTeam().getName() : "N/A")
                .score(evaluation != null ? evaluation.getScore() : null)
                .comment(evaluation != null ? evaluation.getComment() : null)
                .judgeId(judge != null ? judge.getId() : null)
                .judgeName(judge != null ? judge.getName() + " " + judge.getSurname() : null)
                .evaluatedAt(evaluation != null ? evaluation.getEvaluatedAt() : null)
                .lastModifiedAt(evaluation != null ? evaluation.getLastModifiedAt() : null)
                .build();
    }
}
