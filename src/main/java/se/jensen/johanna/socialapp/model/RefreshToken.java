package se.jensen.johanna.socialapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Represents refreshtoken-cookie for token rotation
 */
@Entity
@Table(name = "refresh_tokens")
@NoArgsConstructor
@Getter
@Setter
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refreshTokenId;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private User user;

    @Column(nullable = false)
    Instant expiryDate;

}
