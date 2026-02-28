package unicam.ids.HackHub.model.state;

import unicam.ids.HackHub.dto.requests.submission.HackathonSubmissionEvaluationRequest;
import unicam.ids.HackHub.enums.HackathonState;
import unicam.ids.HackHub.enums.SubmissionState;
import unicam.ids.HackHub.exceptions.InvalidHackathonStateException;
import unicam.ids.HackHub.model.Hackathon;
import unicam.ids.HackHub.model.Submission;
import unicam.ids.HackHub.model.Team;
import unicam.ids.HackHub.model.declareWinner.WinnerStrategy;

import java.util.List;

public class EvaluationState implements unicam.ids.HackHub.model.state.HackathonState {
    @Override
    public void signTeam(Hackathon hackathon, Team team) {
        throw new InvalidHackathonStateException("hackathon in valutazione");
    }

    @Override
    public void unsubscribeTeamToHackathon(Hackathon hackathon, Team team) {
        throw new InvalidHackathonStateException("hackathon in valutazione");
    }

    @Override
    public void evaluateHackathonSubmission(HackathonSubmissionEvaluationRequest request, Submission submission) {
        submission.setScore(request.score());
        submission.setComment(request.comment());
        submission.setState(SubmissionState.VALUTATA);
    }

    @Override
    public Submission createSubmission(String title, String content, Team team) {
        throw new InvalidHackathonStateException("hackathon non in iscrizione");
    }

    @Override
    public void declareWinner(Hackathon hackathon, List<Submission> submissions, WinnerStrategy strategy) {
        Team winnerTeam = strategy.calculateWinner(submissions);
        hackathon.setTeamWinner(winnerTeam);
        hackathon.setState(HackathonState.CONCLUSO);
    }
}
