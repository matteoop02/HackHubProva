package unicam.ids.HackHub.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TEAMS")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @Column(name = "Name", nullable = false, unique = true)
    private String name;

    @OneToOne(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Submission submission;

    @ManyToOne
    @JoinColumn(name = "HackathonId")
    private Hackathon hackathon;

    @OneToOne
    @JoinColumn(name = "LeaderId", referencedColumnName = "Id")
    private User teamLeader;

    @OneToMany(mappedBy = "team")
    private List<User> members = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "TEAM_MENTORS", joinColumns = @JoinColumn(name = "TeamId"), inverseJoinColumns = @JoinColumn(name = "UserId"))
    private List<User> mentors = new ArrayList<>();

    @Column(name = "isPublic", nullable = false)
    private boolean isPublic;

    public void addMember(User user) {
        members.add(user);
    }

    public void addMentor(User user) {
        mentors.add(user);
    }

    public void removeMember(User user) {
        members.remove(user);
    }

    public void removeMentor(User user) {
        mentors.remove(user);
    }
}