package se.jensen.johanna.socialapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.jensen.johanna.socialapp.dto.FriendResponseDTO;
import se.jensen.johanna.socialapp.dto.MyFriendRequest;
import se.jensen.johanna.socialapp.dto.UserListDTO;
import se.jensen.johanna.socialapp.exception.ForbiddenException;
import se.jensen.johanna.socialapp.exception.IllegalFriendshipStateException;
import se.jensen.johanna.socialapp.exception.NotFoundException;
import se.jensen.johanna.socialapp.mapper.FriendshipMapper;
import se.jensen.johanna.socialapp.mapper.UserMapper;
import se.jensen.johanna.socialapp.model.Friendship;
import se.jensen.johanna.socialapp.model.FriendshipStatus;
import se.jensen.johanna.socialapp.model.User;
import se.jensen.johanna.socialapp.repository.FriendshipRepository;
import se.jensen.johanna.socialapp.service.helper.EntityProvider;

import java.util.List;

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
    private final FriendshipMapper friendshipMapper;
    private final UserMapper userMapper;
    private final EntityProvider entityProvider;


    /**
     * Creates a new friendship request with PENDING status.
     * Validates that both users exist and that no friendship or request already exists between them.
     *
     * @param senderId   the ID of the user sending the request
     * @param receiverId the ID of the user intended to receive the request
     * @return a {@link FriendResponseDTO} representing the created request
     * @throws ForbiddenException              if the user attempts to add themselves as a friend
     * @throws IllegalFriendshipStateException if a friendship or request already exists between the users
     * @throws NotFoundException               if either the sender or receiver user is not found
     */

    public FriendResponseDTO sendFriendRequest(Long senderId, Long receiverId) {

        if (senderId.equals(receiverId)) {
            log.warn("User with id={} attempted to send a friend request to themselves", senderId);
            throw new ForbiddenException("You cannot add yourself as a friend.");
        }

        // Check if friendship already exists in either direction
        if (friendshipRepository.existsFriendshipBetween(senderId, receiverId)) {
            log.warn("Duplicate friendship attempt: sender={}, receiver={}", senderId, receiverId);
            throw new IllegalFriendshipStateException("Friendship or request already exists.");
        }

        User sender = entityProvider.getUserOrThrow(senderId);
        User receiver = entityProvider.getUserOrThrow(receiverId);

        Friendship friendship = new Friendship();
        friendship.setSender(sender);
        friendship.setReceiver(receiver);

        friendshipRepository.save(friendship); // Saves the new friendship request
        log.info("User with id={} successfully sent a friend request to user with id={}", senderId, receiverId);
        return friendshipMapper.toFriendResponseDTO(friendship);
    }

    /**
     * Accepts an existing friend request.
     *
     * @param friendshipId  the ID of the friendship relation to accept
     * @param currentUserId the ID of the authenticated user attempting the action
     * @return a {@link FriendResponseDTO} with the updated status (ACCEPTED)
     * @throws NotFoundException               if the friendship is not found
     * @throws ForbiddenException              if the current user is not the receiver of the request
     * @throws IllegalFriendshipStateException if the friendship status is not pending
     */

    public FriendResponseDTO acceptFriendRequest(Long friendshipId, Long currentUserId) {

        Friendship friendship = entityProvider.getFriendshipOrThrow(friendshipId);
        validateReceiver(friendship, currentUserId);

        friendship.accept();
        friendshipRepository.save(friendship);

        log.info("Friend request with id={} accepted by user with id={}", friendshipId, currentUserId);

        return friendshipMapper.toFriendResponseDTO(friendship);
    }

    /**
     * Rejects a pending friend request.
     * Ensures the user rejecting the request is the actual receiver.
     *
     * @param friendshipId  the ID of the friendship relation to reject
     * @param currentUserId the ID of the authenticated user attempting the action
     * @throws NotFoundException               if the friend request is not found
     * @throws ForbiddenException              if the current user is not the receiver of the request
     * @throws IllegalFriendshipStateException if the request has already been accepted or rejected
     */

    public void rejectFriendRequest(Long friendshipId, Long currentUserId) {
        Friendship friendship = entityProvider.getFriendshipOrThrow(friendshipId);
        validateReceiver(friendship, currentUserId);

        friendship.reject();

        log.info("Friendship with id={} rejected by user with id={}", friendshipId, currentUserId);

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
        return friendshipRepository.findFriendshipsByUserIdAndStatus(userId, FriendshipStatus.ACCEPTED)
                .stream().map(f -> {
                    User friend = f.getSender().getUserId().equals(userId) ? f.getReceiver() : f.getSender();
                    return userMapper.toUserListDTO(friend);
                }).toList();

    }

    /**
     * Retrieves a list of pending friendships for the authenticated user.
     * Includes metadata indicating if the request is incoming (user is the receiver).
     *
     * @param userId the ID of the user to fetch friend requests for
     * @return a list of {@link MyFriendRequest} objects
     */

    public List<MyFriendRequest> getFriendRequestsForUser(Long userId) {
        return friendshipRepository.findFriendshipsByUserIdAndStatus(userId, FriendshipStatus.PENDING)
                .stream().map(f -> {
                    User otherFriend = f.getSender().getUserId().equals(userId) ? f.getReceiver() : f.getSender();
                    boolean isIncoming = f.getReceiver().getUserId().equals(userId);

                    return new MyFriendRequest(
                            f.getFriendshipId(),
                            otherFriend.getUserId(),
                            otherFriend.getUsername(),
                            otherFriend.getProfileImagePath(),
                            isIncoming
                    );

                }).toList();
    }


    /**
     * Deletes a friendship relation or a friend request.
     * Ensures that the user requesting the deletion is a participant in the relation.
     *
     * @param friendshipId the ID of the friendship relation to delete
     * @param userId       the ID of the user requesting deletion
     * @throws NotFoundException  if the friendship relation is not found
     * @throws ForbiddenException if the user is not authorized to delete the relation
     */

    public void deleteFriendship(Long friendshipId, Long userId) {
        Friendship friendship = entityProvider.getFriendshipOrThrow(friendshipId);

        validateParticipant(friendship, userId);

        log.info("User with id={} deleted friendship with id={}", userId, friendshipId);

        friendshipRepository.delete(friendship);
    }


    private void validateParticipant(Friendship friendship, Long userId) {
        if (!friendship.getSender().getUserId().equals(userId) && !friendship.getReceiver().getUserId().equals(userId)) {
            log.warn("User with id={} tried to access friendship with id={} without being participant.", userId, friendship.getFriendshipId());
            throw new ForbiddenException("You are not authorized to perform this action");
        }
    }

    private void validateReceiver(Friendship friendship, Long receiverId) {
        if (!friendship.getReceiver().getUserId().equals(receiverId)) {
            log.warn("User with id={} attempted to accept a friend request with id={} but is not the receiver", receiverId, friendship.getFriendshipId());
            throw new ForbiddenException("You are not authorized to perform this action");
        }

    }

}
