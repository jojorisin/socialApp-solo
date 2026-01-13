package se.jensen.johanna.socialapp.model;

import jakarta.persistence.*;

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
}
