package se.jensen.johanna.socialapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.jensen.johanna.socialapp.model.Friendship;
import se.jensen.johanna.socialapp.model.FriendshipStatus;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    boolean existsBySender_UserIdAndReceiver_UserId(Long senderId, Long receiverId);

    List<Friendship> findBySender_UserIdOrReceiver_UserId(Long senderId, Long receiverId);

    List<Friendship> findByReceiver_UserIdAndStatus(Long receiverId, FriendshipStatus status);

    // Finds all friendships where the user is either the sender OR the receiver
    @Query("SELECT f FROM Friendship f WHERE f.sender.userId = :userId OR f.receiver.userId = :userId")
    List<Friendship> findFriendshipsByUserId(@Param("userId") Long userId);

    // Finds friendships for a user filtered by a specific status (e.g., only ACCEPTED)
    @Query("SELECT f FROM Friendship f WHERE (f.sender.userId = :userId OR f.receiver.userId = :userId) AND f.status = :status")
    List<Friendship> findFriendshipsByUserIdAndStatus(@Param("userId") Long userId, @Param("status") FriendshipStatus status);

    // Finds a specific friendship between two users (bi-directional check)
    @Query("SELECT f FROM Friendship f WHERE (f.sender.userId = :user1Id AND f.receiver.userId = :user2Id) OR (f.sender.userId = :user2Id AND f.receiver.userId = :user1Id)")
    List<Friendship> findFriendshipBetween(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
}