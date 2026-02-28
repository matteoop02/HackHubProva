package unicam.ids.HackHub.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "USER_ROLES")
public class UserRole {

    @Id
    @Column(name = "Id", nullable = false)
    private Long id;

    @Size(max = 255)
    @Column(name = "Name", nullable = false)
    private String name;

    @NotNull
    @ColumnDefault("TRUE")
    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;
}