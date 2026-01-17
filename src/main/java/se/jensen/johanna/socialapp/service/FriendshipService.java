package se.jensen.johanna.socialapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.jensen.johanna.socialapp.dto.FriendResponseDTO;
import se.jensen.johanna.socialapp.dto.MyFriendRequest;
import se.jensen.johanna.socialapp.dto.UserListDTO;
import se.jensen.johanna.socialapp.exception.IllegalFriendshipStateException;
import se.jensen.johanna.socialapp.exception.NotFoundException;
import se.jensen.johanna.socialapp.exception.UnauthorizedAccessException;
import se.jensen.johanna.socialapp.mapper.FriendshipMapper;
import se.jensen.johanna.socialapp.mapper.UserMapper;
import se.jensen.johanna.socialapp.model.Friendship;
import se.jensen.johanna.socialapp.model.FriendshipStatus;
import se.jensen.johanna.socialapp.model.User;
import se.jensen.johanna.socialapp.repository.FriendshipRepository;
import se.jensen.johanna.socialapp.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final FriendshipMapper friendshipMapper;
    private final UserMapper userMapper;


    /**
     * Creates a new friendship request with status PENDING.
     * Validates that users exist and that no friendship already exists between them.
     */

    public FriendResponseDTO sendFriendRequest(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("You cannot add yourself as a friend.");
        }

        // Check if friendship already exists in either direction
        if (friendshipRepository.existsBySender_UserIdAndReceiver_UserId(senderId, receiverId) ||
                friendshipRepository.existsBySender_UserIdAndReceiver_UserId(receiverId, senderId)) {
            throw new IllegalStateException("Friendship or request already exists.");
        }

        User sender = userRepository.findById(senderId).orElseThrow(() -> new NotFoundException("Sender with id " + senderId + " not found."));
        User receiver = userRepository.findById(receiverId).orElseThrow(() -> new NotFoundException("Receiver with id " + receiverId + " not found."));

        Friendship friendship = new Friendship();
        friendship.setSender(sender);
        friendship.setReceiver(receiver);
        // Status is PENDING by default

        friendshipRepository.save(friendship); // Saves the new friendship request
        return friendshipMapper.toFriendResponseDTO(friendship);
    }

    /**
     * Accepts an existing friend request.
     * Ensures the user accepting the request is the actual receiver.
     */
    public FriendResponseDTO acceptFriendRequest(
            Long friendshipId,
            Long currentUserId) {

        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new NotFoundException("Friendship with id " + friendshipId + " not found."));

        // Security check: Only the receiver can accept the request
        if (!friendship.getReceiver().getUserId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("You are not authorized to accept this request.");
        }

        // Validation: Cannot accept a request that has been rejected
        if (friendship.getStatus().equals(FriendshipStatus.REJECTED)) {
            throw new IllegalFriendshipStateException("This request has already been rejected");
        }

        // Validation: Cannot accept a request that is already accepted
        if (friendship.getStatus().equals(FriendshipStatus.ACCEPTED)) {
            throw new IllegalFriendshipStateException("This request has already been accepted.");
        }

        friendship.accept(); // Sets status to ACCEPTED and acceptedAt to now
        friendshipRepository.save(friendship); // Updates the current friendship

        return friendshipMapper.toFriendResponseDTO(friendship);
    }

    /**
     * Rejects a pending friend request.
     * Ensures the user rejecting the request is the actual receiver.
     */
    public FriendResponseDTO rejectFriendRequest(Long friendshipId, Long currentUserId) {
        Friendship friendship = friendshipRepository.findById(friendshipId).
                orElseThrow(() -> new NotFoundException("Friendship with id " + friendshipId + " not found."));

        // Security check: Only the receiver can reject the request
        if (!friendship.getReceiver().getUserId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("You are not authorized to reject this request.");
        }

        // Validation: Cannot reject a request that is already accepted
        if (friendship.getStatus().equals(FriendshipStatus.ACCEPTED)) {
            throw new IllegalFriendshipStateException("This request has already been accepted.");
        }
        // Validation: Cannot reject a request that is already rejected
        if (friendship.getStatus().equals(FriendshipStatus.REJECTED)) {
            throw new IllegalFriendshipStateException("This request has already been rejected.");
        }

        friendship.setStatus(FriendshipStatus.REJECTED);
        friendshipRepository.save(friendship);
        return friendshipMapper.toFriendResponseDTO(friendship);
    }

    // Retrieves all friendships (both pending and accepted) for a specific user
    public List<Friendship> getFriendships(Long userId) {
        return friendshipRepository.findBySender_UserIdOrReceiver_UserId(userId, userId);
    }

    // Retrieves only accepted friendships (The "Friends List")
    public List<Friendship> getAcceptedFriendships(Long userId) {
        return friendshipRepository.findFriendshipsByUserIdAndStatus(userId, FriendshipStatus.ACCEPTED);
    }

    /**
     * Retrieves a list of accepted friendships from userId.
     * Filters through the friendships and extracts the "other" user
     *
     * @param userId ID of the user to fetch friends for
     * @return {@link UserListDTO}
     */
    public List<UserListDTO> getFriendsForUser(Long userId) {
        List<Friendship> friendships = friendshipRepository.findFriendshipsByUserIdAndStatus(userId, FriendshipStatus.ACCEPTED);

        List<UserListDTO> friends = new ArrayList<>();
        for (Friendship f : friendships) {
            User friend = f.getSender().getUserId().equals(userId) ?
                    f.getReceiver() : f.getSender();
            friends.add(userMapper.toUserListDTO(friend));
        }
        return friends;
    }

    /**
     * Retrieves a list of pending friendships for authenticated user
     * Filters list and returns the other user in the relation
     * boolean isIncoming is true when the user is the receiver
     *
     * @param userId ID of user to fetch friendrequests for
     * @return {@link MyFriendRequest}
     */
    public List<MyFriendRequest> getFriendRequestsForUser(Long userId) {
        List<Friendship> pendingFriends = friendshipRepository.findFriendshipsByUserIdAndStatus(userId, FriendshipStatus.PENDING);
        List<MyFriendRequest> friendRequests = new ArrayList<>();
        for (Friendship f : pendingFriends) {
            User otherUser = f.getSender().getUserId().equals(userId) ?
                    f.getReceiver() : f.getSender();

            boolean isIncoming = f.getReceiver().getUserId().equals(userId);

            friendRequests.add(new MyFriendRequest(
                    f.getFriendshipId(),
                    otherUser.getUserId(),
                    otherUser.getUsername(),
                    otherUser.getProfileImagePath(),
                    isIncoming
            ));

        }
        return friendRequests;

    }

    // Retrieves pending requests involving the user
    public List<Friendship> getPendingFriendships(Long userId) {
        return friendshipRepository.findFriendshipsByUserIdAndStatus(userId, FriendshipStatus.PENDING);
    }

    public void deleteFriendship(Long friendshipId, Long userId) {
        Friendship friendship = friendshipRepository.findById(friendshipId).orElseThrow(NotFoundException::new);
        if (!userId.equals(friendship.getSender().getUserId()) && !userId.equals(friendship.getReceiver().getUserId())) {
            throw new UnauthorizedAccessException("You are not authorized to delete this friendship");
        }

        friendshipRepository.delete(friendship);
    }
}
