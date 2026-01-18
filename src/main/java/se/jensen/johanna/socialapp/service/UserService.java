package se.jensen.johanna.socialapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import se.jensen.johanna.socialapp.dto.*;
import se.jensen.johanna.socialapp.dto.admin.*;
import se.jensen.johanna.socialapp.exception.NotFoundException;
import se.jensen.johanna.socialapp.exception.NotUniqueException;
import se.jensen.johanna.socialapp.exception.PasswordMisMatchException;
import se.jensen.johanna.socialapp.mapper.UserMapper;
import se.jensen.johanna.socialapp.model.Role;
import se.jensen.johanna.socialapp.model.User;
import se.jensen.johanna.socialapp.repository.UserRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final FriendshipService friendshipService;
    private final PostService postService;


    public RegisterUserResponse registerUser(RegisterUserRequest registerUserRequest) {
        log.info("Trying to register new user with email={}", registerUserRequest.email());
        validateCredentials(registerUserRequest);

        String hashedPw = passwordEncoder.encode(registerUserRequest.password());
        User user = userMapper.toUser(registerUserRequest, hashedPw, Role.MEMBER);
        userRepository.save(user);

        log.info("New user registered with id={} and email={}", user.getUserId(), user.getEmail());

        RegisterUserResponse response = new RegisterUserResponse();
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setUserId(user.getUserId());
        return response;
    }

    public UpdateUserResponse updateUser(UpdateUserRequest userRequest, Long userId) {
        log.info("Trying to update user with id={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Could not update user - user with id={} not found", userId);
                    return new NotFoundException();
                });
        userMapper.updateUser(userRequest, user);
        userRepository.save(user);

        log.info("User with id={} updated", userId);
        return userMapper.toUpdateUserResponse(user);


    }


    public List<UserListDTO> findAllUsers() {
        return userRepository.findAllUsersByRole(Role.MEMBER).stream()
                .map(userMapper::toUserListDTO).toList();

    }


    public List<AdminUserDTO> findAllUsersAdmin() {
        return userRepository.findAll().stream()
                .map(userMapper::toAdminUserDTO).toList();
    }

    public AdminUserDTO findUserAdmin(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(NotFoundException::new);

        return userMapper.toAdminUserDTO(user);
    }

    public UserDTO findUser(Long userId) {
        return userRepository.findById(userId)
                .map(userMapper::toUserDTO).orElseThrow(NotFoundException::new);
    }

    public void deleteUser(Long userId) {
        log.info("Trying to delete user with id={}", userId);
        User userToDelete = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("Could not remove - user with id={} was not found", userId);
            return new NotFoundException();
        });
        userRepository.delete(userToDelete);
        log.info("User with id={} removed", userId);
    }

    /**
     * Updates and saves Role for user by Admin
     *
     * @param request {@link RoleRequest} Contains email of user to update and type of role
     * @return {@link RoleResponse}
     */
    public RoleResponse addRole(RoleRequest request) {
        User user = userRepository.findByEmail(request.email()).orElseThrow(() -> {
            log.warn("Could not update role - user with email={} not found", request.email());
            return new NotFoundException();
        });
        log.info("Admin role-update initiated for user with email={}", user.getEmail());
        user.setRole(request.role());
        userRepository.save(user);
        return new RoleResponse(user.getEmail(), user.getRole());
    }


    public AdminUpdateUserResponse updateUserAdmin(AdminUpdateUserRequest userRequest,
                                                   Long userId) {
        log.info("Admin update initiated for user with id={}", userId);
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("Could not update User - User with id={} not found", userId);
            return new NotFoundException();
        });
        userMapper.updateUserAdmin(userRequest, user);
        userRepository.save(user);

        return userMapper.toAdminResponse(user);

    }

    public void validateCredentials(RegisterUserRequest registerUserRequest) {
        if (!registerUserRequest.password().equals(registerUserRequest.confirmPassword())) {
            log.warn("Password mismatch during registration for email={}", registerUserRequest.email());
            throw new PasswordMisMatchException();
        }
        if (userRepository.existsByEmail(registerUserRequest.email())) {
            log.warn("Registration attempt with already registered email={}", registerUserRequest.email());
            throw new NotUniqueException("Email is already registered. Log in or try different email.");
        }
        if (userRepository.existsByUsername(registerUserRequest.username())) {
            log.warn("Registration attempt with already taken username={}", registerUserRequest.username());
            throw new NotUniqueException("Username is already registered. Please choose a unique username.");
        }
    }



    public HomePageResponse getProfile(Long userId){
        UserDTO userDTO = findUser(userId);
        List<UserListDTO> friends = friendshipService.getFriendsForUser(userId);
        List<PostResponse> posts = postService.getPostsForUser(userId);

        return new HomePageResponse(
                "Profilsida",
                userDTO.username(),
                userDTO.profileImagePath(),
                posts,
                friends);
    }


}
