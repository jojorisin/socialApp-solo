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

    @Enumerated(EnumType.STRING)
    private FriendshipStatus status = FriendshipStatus.PENDING;

    private LocalDateTime acceptedAt;

    /**
     * Updates the friendship status to ACCEPTED and records the current timestamp.
     * This should be called when a user accepts a friend request.
     */
    public void accept() {
        this.status = FriendshipStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }

    public Long getFriendshipId() {
        return friendshipId;
    }

    public void setFriendshipId(Long friendshipId) {
        this.friendshipId = friendshipId;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    public void setStatus(FriendshipStatus status) {
        this.status = status;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }
}
