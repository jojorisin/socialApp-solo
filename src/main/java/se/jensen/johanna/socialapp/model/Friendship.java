package se.jensen.johanna.socialapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
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

    public void accept() {
        this.status = FriendshipStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }
}
