package se.jensen.johanna.socialapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.jensen.johanna.socialapp.dto.FriendResponseDTO;
import se.jensen.johanna.socialapp.dto.FriendshipStatusDTO;
import se.jensen.johanna.socialapp.dto.MyFriendRequest;
import se.jensen.johanna.socialapp.dto.UserListDTO;
import se.jensen.johanna.socialapp.exception.ForbiddenException;
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
import java.util.Optional;

/**
 * Service class for managing friendship relations between users in the application.
 * <p>
 * This class provides functionality to send friend requests, accept or reject them,
 * retrieve lists of friends, and handle the deletion of existing relationships.
 * All operations are transactional and include security checks to ensure data integrity.
 */

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final FriendshipMapper friendshipMapper;
    private final UserMapper userMapper;


    /**
     * Creates a new friendship request with PENDING status.
     * Validates that both users exist and that no friendship or request already exists between them.
     *
     * @param senderId   the ID of the user sending the request
     * @param receiverId the ID of the user intended to receive the request
     * @return a {@link FriendResponseDTO} representing the created request
     * @throws IllegalArgumentException if the user attempts to add themselves as a friend
     * @throws IllegalStateException    if a friendship or request already exists between the users
     * @throws NotFoundException        if either the sender or receiver user is not found
     */

    public FriendResponseDTO sendFriendRequest(Long senderId, Long receiverId) {
        log.info("User with id={} is trying to send a friend request to user with id={}", senderId, receiverId);

        if (senderId.equals(receiverId)) {
            log.warn("User with id={} attempted to send a friend request to themselves", senderId);
            throw new IllegalFriendshipStateException("You cannot add yourself as a friend.");
        }

        // Check if friendship already exists in either direction
        if (friendshipRepository.existsBySender_UserIdAndReceiver_UserId(senderId, receiverId) ||
                friendshipRepository.existsBySender_UserIdAndReceiver_UserId(receiverId, senderId)) {
            log.warn("User with id={} attempted to send a duplicate friend request to user with id={}", senderId, receiverId);
            throw new IllegalFriendshipStateException("Friendship or request already exists.");
        }

        User sender = userRepository.findById(senderId).orElseThrow(() -> {
            log.warn("Sender with id={} not found when sending a friend request", senderId);
            return new NotFoundException("Sender with id " + senderId + " not found.");
        });
        User receiver = userRepository.findById(receiverId).orElseThrow(() -> {
            log.warn("Receiver with id={} not found when receiving friend request", receiverId);
            return new NotFoundException("Receiver with id " + receiverId + " not found.");
        });

        Friendship friendship = new Friendship();
        friendship.setSender(sender);
        friendship.setReceiver(receiver);
        // Status is PENDING by default

        friendshipRepository.save(friendship); // Saves the new friendship request
        log.info("User with id={} successfully sent a friend request to user with id={}", senderId, receiverId);
        return friendshipMapper.toFriendResponseDTO(friendship);
    }

    /**
     * Accepts an existing friend request.
     * Ensures the user accepting the request is the actual receiver and that the request is in a valid state.
     *
     * @param friendshipId  the ID of the friendship relation to accept
     * @param currentUserId the ID of the authenticated user attempting the action
     * @return a {@link FriendResponseDTO} with the updated status (ACCEPTED)
     * @throws NotFoundException               if the friend request is not found
     * @throws UnauthorizedAccessException     if the current user is not the receiver of the request
     * @throws IllegalFriendshipStateException if the request has already been accepted or rejected
     */

    public FriendResponseDTO acceptFriendRequest(
            Long friendshipId,
            Long currentUserId) {
        log.info("User with id={} is attempting to accept a friend request with id={}", currentUserId, friendshipId);

        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> {
                    log.warn("Friend request with id={} not found when user with id={} attempted to accept it", friendshipId, currentUserId);
                    return new NotFoundException("Friendship with id " + friendshipId + " not found.");
                });

        // Security check: Only the receiver can accept the request
        if (!friendship.getReceiver().getUserId().equals(currentUserId)) {
            log.warn("User with id={} attempted to accept friend request with id={} but is not the receiver", currentUserId, friendshipId);
            throw new ForbiddenException("You are not authorized to accept this request.");
        }

        // Validation: Cannot accept a request that has been rejected
        if (friendship.getStatus().equals(FriendshipStatus.REJECTED)) {
            log.warn("User with id={} attempted to accept rejected friend request with id={}", currentUserId, friendshipId);
            throw new IllegalFriendshipStateException("This request has already been rejected");
        }

        // Validation: Cannot accept a request that is already accepted
        if (friendship.getStatus().equals(FriendshipStatus.ACCEPTED)) {
            log.warn("User with id={} attempted to accept already accepted friend request with id={}", currentUserId, friendshipId);
            throw new IllegalFriendshipStateException("This request has already been accepted.");
        }

        friendship.accept(); // Sets status to ACCEPTED and acceptedAt to now
        friendshipRepository.save(friendship); // Updates the current friendship

        log.info("Friend request with id={} successfully accepted by user with id={}", friendshipId, currentUserId);

        return friendshipMapper.toFriendResponseDTO(friendship);
    }

    /**
     * Rejects a pending friend request.
     * Ensures the user rejecting the request is the actual receiver.
     *
     * @param friendshipId  the ID of the friendship relation to reject
     * @param currentUserId the ID of the authenticated user attempting the action
     * @throws NotFoundException               if the friend request is not found
     * @throws UnauthorizedAccessException     if the current user is not the receiver of the request
     * @throws IllegalFriendshipStateException if the request has already been accepted or rejected
     */

    public void rejectFriendRequest(Long friendshipId, Long currentUserId) {
        log.info("User with id={} is attempting to reject a friend request with id={}", currentUserId, friendshipId);

        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> {
                    log.warn("Friend request with id={} not found when user with id={} attempted to reject it", friendshipId, currentUserId);
                    return new NotFoundException("Friendship with id " + friendshipId + " not found.");
                });

        // Security check: Only the receiver can reject the request
        if (!friendship.getReceiver().getUserId().equals(currentUserId)) {
            log.warn("User with id={} attempted to reject friend request with id={} but is not the receiver", currentUserId, friendshipId);
            throw new ForbiddenException("You are not authorized to reject this request.");
        }

        // Validation: Cannot reject a request that is already accepted
        if (friendship.getStatus().equals(FriendshipStatus.ACCEPTED)) {
            log.warn("User with id={} attempted to reject already accepted friend request with id={}", currentUserId, friendshipId);
            throw new IllegalFriendshipStateException("This request has already been accepted.");
        }
        // Validation: Cannot reject a request that is already rejected
        if (friendship.getStatus().equals(FriendshipStatus.REJECTED)) {
            log.warn("User with id={} attempted to reject already rejected friend request with id={}", currentUserId, friendshipId);
            throw new IllegalFriendshipStateException("This request has already been rejected.");
        }

        friendship.setStatus(FriendshipStatus.REJECTED);
        log.info("Friend request with id={} successfully rejected by user with id={}", friendshipId, currentUserId);


        friendshipRepository.delete(friendship);


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
     * Retrieves a list of pending friendships for the authenticated user.
     * Includes metadata indicating if the request is incoming (user is the receiver).
     *
     * @param userId the ID of the user to fetch friend requests for
     * @return a list of {@link MyFriendRequest} objects
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

    /**
     * Retrieves the friendship status between the current user and a target user.
     * Used to determine UI state (Add Friend, Cancel Request, Accept/Reject, Unfriend).
     */
    public FriendshipStatusDTO getFriendshipStatus(Long currentUserId, Long targetUserId) {
        List<Friendship> friendships = friendshipRepository.findFriendshipBetween(currentUserId, targetUserId);

        if (friendships.isEmpty()) {
            return null; // No relationship exists
        }

        // If multiple exist due to bad data, we just take the first one to avoid crashing
        Friendship f = friendships.get(0);
        boolean isIncoming = f.getReceiver().getUserId().equals(currentUserId);

        return new FriendshipStatusDTO(f.getFriendshipId(), f.getStatus(), isIncoming);
    }

    /**
     * Deletes a friendship relation or a friend request.
     * Ensures that the user requesting the deletion is a participant in the relation.
     *
     * @param friendshipId the ID of the friendship relation to delete
     * @param userId       the ID of the user requesting deletion
     * @throws NotFoundException           if the friendship relation is not found
     * @throws UnauthorizedAccessException if the user is not authorized to delete the relation
     */

    public void deleteFriendship(Long friendshipId, Long userId) {
        log.info("User with id={} is attempting to delete a friendship with id={}", userId, friendshipId);

        Friendship friendship = friendshipRepository.findById(friendshipId).orElseThrow(() -> {
            log.warn("Friendship with id={} not found when user with id={} attempted to delete it", friendshipId, userId);
            return new NotFoundException("Friendship with id " + friendshipId + " not found.");
        });
        if (!userId.equals(friendship.getSender().getUserId()) && !userId.equals(friendship.getReceiver().getUserId())) {
            log.warn("User with id={} attempted to delete friendship with id={} but is not authorized", userId, friendshipId);
            throw new UnauthorizedAccessException("You are not authorized to delete this friendship");
        }

        friendshipRepository.delete(friendship);
        log.info("User with id={} successfully deleted friendship with id={}", userId, friendshipId);
    }
}
