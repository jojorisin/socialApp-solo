package se.jensen.johanna.socialapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.jensen.johanna.socialapp.model.Friendship;
import se.jensen.johanna.socialapp.model.FriendshipStatus;

import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    
    boolean existsBySender_UserIdAndReceiver_UserId(Long senderId, Long receiverId);

    List<Friendship> findBySender_UserIdOrReceiver_UserId(Long senderId, Long receiverId);
    
    List<Friendship> findByReceiver_UserIdAndStatus(Long receiverId, FriendshipStatus status);
}