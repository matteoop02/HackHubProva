package unicam.ids.HackHub.model;

import jakarta.persistence.*;
import lombok.*;
import unicam.ids.HackHub.enums.TeamRole;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "TEAM_MEMBERSHIPS",
        uniqueConstraints = @UniqueConstraint(name = "uk_team_membership_user_team", columnNames = {"UserId", "TeamId"}))
public class TeamMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "UserId", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "TeamId", nullable = false)
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(name = "Role", nullable = false, length = 30)
    private TeamRole role;
}
