package unicam.ids.HackHub.model.state;

import unicam.ids.HackHub.dto.requests.submission.HackathonSubmissionEvaluationRequest;
import unicam.ids.HackHub.model.Hackathon;
import unicam.ids.HackHub.model.Submission;
import unicam.ids.HackHub.model.Team;
import unicam.ids.HackHub.model.declareWinner.WinnerStrategy;

import java.util.List;

public interface HackathonState {
    void signTeam(Hackathon hackathon, Team team);

    void unsubscribeTeamToHackathon(Hackathon hackathon, Team team);

    void evaluateHackathonSubmission(HackathonSubmissionEvaluationRequest request, Submission submission);

    Submission createSubmission(String title, String content, Team team);

    void declareWinner(Hackathon hackathon, List<Submission> submissions, WinnerStrategy strategy);

}
