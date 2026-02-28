package unicam.ids.HackHub.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "USERS")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Column(name = "Name", nullable = false)
    private String name;

    @Size(max = 255)
    @NotNull
    @Column(name = "Surname", nullable = false)
    private String surname;

    @Size(max = 255)
    @NotNull
    @Column(name = "Username", nullable = false, unique = true)
    private String username;

    @Size(max = 255)
    @NotNull
    @Column(name = "Email", nullable = false, unique = true)
    private String email;

    @Size(max = 500)
    @NotNull
    @Column(name = "Password", nullable = false, length = 500)
    private String password;

    @Temporal(TemporalType.DATE)
    @Column(name = "DateOfBirth")
    private Date dateOfBirth;

    @ManyToOne
    @JoinColumn(name = "RoleId", nullable = false)
    private UserRole role;

    @ManyToOne
    @JoinColumn(name = "TeamId", nullable = true)
    private Team team;

    @NotNull
    @ColumnDefault("FALSE")
    @Column(name = "IS_DELETED", nullable = false)
    private Boolean isDeleted = false;
}