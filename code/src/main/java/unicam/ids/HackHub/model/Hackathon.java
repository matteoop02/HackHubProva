package unicam.ids.HackHub.model;

import jakarta.persistence.*;
import lombok.*;
import unicam.ids.HackHub.enums.HackathonState;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "HACKATHONS")
public class Hackathon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Name", nullable = false, unique = true)
    private String name;

    @Column(name = "Place")
    private String place;

    @Column(name = "Regulation", length = 2000)
    private String regulation;

    @Column(name = "SubscriptionDeadline")
    private LocalDateTime subscriptionDeadline;

    @Column(name = "StartDate")
    private LocalDateTime startDate;

    @Column(name = "EndDate")
    private LocalDateTime endDate;

    @Column(name = "Reward")
    private Double reward;

    @Column(name = "MaxTeamSize")
    private Integer maxTeamSize;

    @Column(name = "IsPublic")
    private Boolean isPublic;

    @Enumerated(EnumType.STRING)
    @Column(name = "State")
    private HackathonState state;

    @ManyToOne
    @JoinColumn(name = "Organizer_Id", nullable = false)
    private User organizer;

    @ManyToOne
    @JoinColumn(name = "JudgeId")
    private User judge;

    @ManyToOne
    @JoinColumn(name = "TeamWinner_Id")
    private Team teamWinner;

    @ManyToMany
    @JoinTable(name = "HACKATHON_MENTORS",
            joinColumns = @JoinColumn(name = "HackathonId"),
            inverseJoinColumns = @JoinColumn(name = "UserId"))
    private Set<User> mentors = new HashSet<>();

    @OneToMany(mappedBy = "hackathon", cascade = CascadeType.ALL)
    private Set<Team> teams = new HashSet<>();

}
