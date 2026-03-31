package unicam.ids.HackHub.repository;

import unicam.ids.HackHub.enums.InviteState;
import unicam.ids.HackHub.model.InviteInsidePlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import unicam.ids.HackHub.model.Team;
import unicam.ids.HackHub.model.User;

import java.util.List;

@Repository
public interface InsideInviteRepository extends JpaRepository<InviteInsidePlatform, Long> {

    List<InviteInsidePlatform> findByRecipientUserAndStatus(User recipientUser, InviteState status);

    List<InviteInsidePlatform> findByTeamAndStatus(Team team, InviteState status);
    boolean existsByRecipientUserAndTeamAndStatus(User recipientUser, Team team, InviteState status);
}
