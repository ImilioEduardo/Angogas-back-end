package ao.angogas.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_verification_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expira_em", nullable = false)
    private OffsetDateTime expiraEm;

    @Column(nullable = false)
    @Builder.Default
    private boolean usado = false;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private OffsetDateTime criadoEm;
}
