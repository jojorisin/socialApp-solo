package se.jensen.johanna.socialapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.jensen.johanna.socialapp.model.Friendship;
import se.jensen.johanna.socialapp.model.FriendshipStatus;

import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    
    boolean existsBySender_UserIdAndReceiver_UserId(Long senderId, Long receiverId);

    List<Friendship> findBySender_UserIdOrReceiver_UserId(Long senderId, Long receiverId);
    
    List<Friendship> findByReceiver_UserIdAndStatus(Long receiverId, FriendshipStatus status);

    @Query("SELECT f FROM Friendship f WHERE f.sender.userId = :userId OR f.receiver.userId = :userId")
    List<Friendship> findFriendshipsByUserId(@Param("userId") Long userId);

    @Query("SELECT f FROM Friendship f WHERE (f.sender.userId = :userId OR f.receiver.userId = :userId) AND f.status = :status")
    List<Friendship> findFriendshipsByUserIdAndStatus(@Param("userId") Long userId, @Param("status") FriendshipStatus status);
}