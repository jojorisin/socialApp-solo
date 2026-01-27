package se.jensen.johanna.socialapp.controller;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.jensen.johanna.socialapp.dto.UserDTO;
import se.jensen.johanna.socialapp.dto.UserListDTO;
import se.jensen.johanna.socialapp.dto.UserPostDTO;
import se.jensen.johanna.socialapp.service.FriendshipService;
import se.jensen.johanna.socialapp.service.PostService;
import se.jensen.johanna.socialapp.service.UserService;

import java.util.List;

/**
 * REST controller for managing user-related operations.
 * Provides endpoints for retrieving user lists, individual profiles, and user friendships.
 */

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final FriendshipService friendshipService;
    private final PostService postService;

    /**
     * Searches for users by their username with support for pagination.
     * * <p>
     * * The search is case-insensitive and matches any username that contains
     * * the provided search string. Results are returned in a paginated format
     * * to ensure high performance and low network overhead.
     * * </p>
     *
     * @param username Content to search
     * @param pageable Pagination and sorting information
     * @return Paginated list of UserDTO
     */
    @GetMapping("/search")
    public ResponseEntity<Page<UserDTO>> searchUsers(
            @RequestParam("q") String username,
            @ParameterObject @PageableDefault(size = 10, sort = "username", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<UserDTO> userDtos = userService.searchUsers(username, pageable);
        return ResponseEntity.ok(userDtos);
    }

    /**
     * Retrieves a list of all users with the MEMBER role.
     * The returned list contains simplified user information.
     *
     * @return a list of UserListDTO objects
     */
    @GetMapping
    public ResponseEntity<List<UserListDTO>> getAllUsers() {
        List<UserListDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Retrieves detailed profile information for a specific user.
     *
     * @param userId the ID of the user to retrieve
     * @return the UserDTO containing user details
     */

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long userId) {
        UserDTO userDTO = userService.getUser(userId);
        return ResponseEntity.ok(userDTO);

    }

    @GetMapping("/{userId}/posts")
    public ResponseEntity<Page<UserPostDTO>> getUserPosts(
            @PathVariable Long userId,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserPostDTO> userPosts = postService.getPostsForUser(userId, pageable);
        return ResponseEntity.ok(userPosts);


    }

    /**
     * Retrieves a list of accepted friendships to a specific user
     *
     * @param userId ID of user to fetch friends for
     * @return {@link UserListDTO}
     */
    @GetMapping("/{userId}/friends")
    public ResponseEntity<List<UserListDTO>> getUserFriends(@PathVariable Long userId) {

        List<UserListDTO> friends = friendshipService.getFriendsForUser(userId);


        return ResponseEntity.ok(friends);
    }


}
