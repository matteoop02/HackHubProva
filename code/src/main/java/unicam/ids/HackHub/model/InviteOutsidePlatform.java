package unicam.ids.HackHub.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import unicam.ids.HackHub.enums.InviteState;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "outside_invites", indexes = {
        @Index(name = "idx_invite_token", columnList = "invite_token"),
        @Index(name = "idx_recipient_email", columnList = "recipient_email")
})
public class InviteOutsidePlatform implements Invite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "senderUserId", nullable = false)
    private User senderUser;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Column(name = "invite_token", nullable = false, unique = true, length = 64)
    private String inviteToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InviteState status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(length = 1000)
    private String message;

    @Override
    public void setStatus(InviteState status) {
        this.status = status;
    }

    @Override
    public void send() {
        this.status.createBehavior().send(this);
    }

    @Override
    public void accept() {
        this.status.createBehavior().accept(this);
    }

    @Override
    public void reject() {
        this.status.createBehavior().reject(this);
    }

    @Override
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt) && status == InviteState.IN_ATTESA;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
