package se.jensen.johanna.socialapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.jensen.johanna.socialapp.model.Friendship;
import se.jensen.johanna.socialapp.model.FriendshipStatus;

import java.util.List;

/**
 * Repository interface for {@link Friendship} entities.
 * Provides methods for managing and querying friendship relationships between users.
 */
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {


    @Query("SELECT COUNT(f) > 0 FROM Friendship f WHERE " +
            "(f.sender.userId = :userId1 AND f.receiver.userId = :userId2) OR " +
            "(f.sender.userId = :userId2 AND f.receiver.userId = :userId1)")
    boolean existsFriendshipBetween(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    // Finds friendships for a user filtered by a specific status (e.g., only ACCEPTED)
    @Query("SELECT f FROM Friendship f WHERE (f.sender.userId = :userId OR f.receiver.userId = :userId) AND f.status = :status")
    List<Friendship> findFriendshipsByUserIdAndStatus(@Param("userId") Long userId, @Param("status") FriendshipStatus status);

}