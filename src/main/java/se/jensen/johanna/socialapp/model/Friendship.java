package se.jensen.johanna.socialapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import se.jensen.johanna.socialapp.exception.IllegalFriendshipStateException;

import java.time.LocalDateTime;

/**
 * Represents a friendship in the social-app
 * Contains factory-methods to ensure safe acceptance of friendship requests
 * and relations to friends as sender and receiver for proper backend-logic
 */
@Entity
@Table(name = "friendships", uniqueConstraints = {@UniqueConstraint(columnNames = {"sender_id", "receiver_id"})})
@Getter
@Setter
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long friendshipId;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @Enumerated(EnumType.STRING)
    private FriendshipStatus status = FriendshipStatus.PENDING;

    private LocalDateTime acceptedAt;

    /**
     * Updates the friendship status to ACCEPTED and records the current timestamp.
     * This should be called when a user accepts a friend request.
     */
    public void accept() {
        if (this.status != FriendshipStatus.PENDING) {
            throw new IllegalFriendshipStateException("This request has already been handled");
        }
        this.status = FriendshipStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }

    public void reject() {
        if (this.status != FriendshipStatus.PENDING) {
            throw new IllegalFriendshipStateException("This request has already been handled");
        }
        this.status = FriendshipStatus.REJECTED;
    }


}
