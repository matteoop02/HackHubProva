package unicam.ids.HackHub.model.state;

import unicam.ids.HackHub.dto.requests.submission.HackathonSubmissionEvaluationRequest;
import unicam.ids.HackHub.exceptions.InvalidHackathonStateException;
import unicam.ids.HackHub.model.Hackathon;
import unicam.ids.HackHub.model.Submission;
import unicam.ids.HackHub.model.Team;
import unicam.ids.HackHub.model.declareWinner.WinnerStrategy;

import java.util.List;

public class RunningState implements HackathonState {
    @Override
    public void signTeam(Hackathon hackathon, Team team) {
        throw new InvalidHackathonStateException("Iscrizioni chiuse: hackathon in corso");
    }

    @Override
    public void unsubscribeTeamToHackathon(Hackathon hackathon, Team team) {
        hackathon.getTeams().remove(team);
    }

    @Override
    public void evaluateHackathonSubmission(HackathonSubmissionEvaluationRequest request, Submission submission) {
        throw new InvalidHackathonStateException("hackathon non in valutazione");
    }

    @Override
    public Submission createSubmission(String title, String content, Team team) {
        throw new InvalidHackathonStateException("hackathon non in iscrizione");
    }

    @Override
    public void declareWinner(Hackathon hackathon, List<Submission> submissions, WinnerStrategy strategy) {
        throw new InvalidHackathonStateException("Hackathon in corso: impossibile dichiarare il vincitore");
    }
}
