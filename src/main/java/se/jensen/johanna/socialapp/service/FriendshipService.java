package se.jensen.johanna.socialapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.jensen.johanna.socialapp.exception.NotFoundException;
import se.jensen.johanna.socialapp.model.Friendship;
import se.jensen.johanna.socialapp.model.FriendshipStatus;
import se.jensen.johanna.socialapp.model.User;
import se.jensen.johanna.socialapp.repository.FriendshipRepository;
import se.jensen.johanna.socialapp.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public void sendFriendRequest(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("You cannot add yourself as a friend.");
        }

        // Check if friendship already exists in either direction
        if (friendshipRepository.existsBySender_UserIdAndReceiver_UserId(senderId, receiverId) ||
            friendshipRepository.existsBySender_UserIdAndReceiver_UserId(receiverId, senderId)) {
            throw new IllegalStateException("Friendship or request already exists.");
        }

        User sender = userRepository.findById(senderId).orElseThrow(NotFoundException::new);
        User receiver = userRepository.findById(receiverId).orElseThrow(NotFoundException::new);

        Friendship friendship = new Friendship();
        friendship.setSender(sender);
        friendship.setReceiver(receiver);
        // Status is PENDING by default

        friendshipRepository.save(friendship);
    }

    public void acceptFriendRequest(Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(NotFoundException::new);

        friendship.accept(); // Sets status to ACCEPTED and acceptedAt to now
        friendshipRepository.save(friendship);
    }

    public List<Friendship> getFriendships(Long userId) {
        return friendshipRepository.findBySender_UserIdOrReceiver_UserId(userId, userId);
    }

    public List<Friendship> getAcceptedFriendships(Long userId) {
        return friendshipRepository.findFriendshipsByUserIdAndStatus(userId, FriendshipStatus.ACCEPTED);
    }

    public List<Friendship> getPendingFriendships(Long userId) {
        return friendshipRepository.findFriendshipsByUserIdAndStatus(userId, FriendshipStatus.PENDING);
    }
}
