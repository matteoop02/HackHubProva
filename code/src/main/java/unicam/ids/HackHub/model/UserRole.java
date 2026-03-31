package unicam.ids.HackHub.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "USER_ROLES")
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Column(name = "Name", nullable = false, unique = true)
    private String name;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;
}
