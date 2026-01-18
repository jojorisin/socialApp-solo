package se.jensen.johanna.socialapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.jensen.johanna.socialapp.dto.HomePageResponse;
import se.jensen.johanna.socialapp.dto.UserDTO;
import se.jensen.johanna.socialapp.dto.UserListDTO;
import se.jensen.johanna.socialapp.service.FriendshipService;
import se.jensen.johanna.socialapp.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final FriendshipService friendshipService;

    //OBS vilka är för admin vilka för user


    //Hämtar alla users med role MEMBER.
    //Userlist innehåller mindre info
    @GetMapping
    public ResponseEntity<List<UserListDTO>> getAllUsers() {
        List<UserListDTO> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    //Visar användarprofil
    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long userId) {
        UserDTO userDTO = userService.findUser(userId);
        return ResponseEntity.ok(userDTO);

    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{userId}/profile")
    public ResponseEntity<HomePageResponse> getProfile(@PathVariable Long userId){

        HomePageResponse homePage = userService.getProfile(userId);

        return ResponseEntity.ok(homePage);
    }


    /**
     * Retrieves a list of accepted friendships to a specific user
     *
     * @param userId ID of user to fetch friends for
     * @return {@link UserListDTO}
     */
    @GetMapping("/{userId}/friends")
    public ResponseEntity<List<UserListDTO>> getMyFriends(@PathVariable Long userId) {

        List<UserListDTO> friends = friendshipService.getFriendsForUser(userId);


        return ResponseEntity.ok(friends);
    }


}
