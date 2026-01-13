package se.jensen.johanna.socialapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long friendshipId;

    @ManyToOne
    private User sender;

    @ManyToOne
    private User receiver;

    private FriendshipStatus status = FriendshipStatus.PENDING;

    private LocalDateTime acceptedAt;

    // Funktionen körs igång när user accepterar friend request.
    public void accept() {
        this.status = FriendshipStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }
}
