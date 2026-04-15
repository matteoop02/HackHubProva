package unicam.ids.HackHub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import unicam.ids.HackHub.enums.HackathonRole;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "HACKATHON_ROLE_ASSIGNMENTS",
        uniqueConstraints = @UniqueConstraint(name = "uk_hackathon_role_assignment",
                columnNames = {"UserId", "HackathonId", "Role"}))
public class HackathonRoleAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "UserId", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "HackathonId", nullable = false)
    private Hackathon hackathon;

    @Enumerated(EnumType.STRING)
    @Column(name = "Role", nullable = false, length = 30)
    private HackathonRole role;
}
