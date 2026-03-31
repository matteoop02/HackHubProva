package unicam.ids.HackHub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import unicam.ids.HackHub.enums.HackathonState;
import unicam.ids.HackHub.model.Hackathon;
import unicam.ids.HackHub.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HackathonRepository extends JpaRepository<Hackathon, Long> {
    List<Hackathon> findAllByIsPublic(Boolean isPublic);
}
