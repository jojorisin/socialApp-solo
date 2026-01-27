package se.jensen.johanna.socialapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import se.jensen.johanna.socialapp.dto.*;
import se.jensen.johanna.socialapp.dto.admin.RoleRequest;
import se.jensen.johanna.socialapp.dto.admin.RoleResponse;
import se.jensen.johanna.socialapp.exception.NotFoundException;
import se.jensen.johanna.socialapp.exception.NotUniqueException;
import se.jensen.johanna.socialapp.exception.PasswordMisMatchException;
import se.jensen.johanna.socialapp.mapper.UserMapper;
import se.jensen.johanna.socialapp.model.Role;
import se.jensen.johanna.socialapp.model.User;
import se.jensen.johanna.socialapp.repository.UserRepository;

import java.util.List;

/**
 * Service class responsible for managing user-related operations.
 * This includes user registration, profile management, administrative updates,
 * and retrieval of user-specific data such as profiles and friend lists.
 */

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    public Page<UserDTO> searchUsers(String username, Pageable pageable) {
        return userRepository.findByUsernameContainingIgnoreCase(username, pageable).map(userMapper::toUserDTO);

    }

    /**
     * Registers a new user in the system.
     * Validates credentials, hashes the password, and assigns the default MEMBER role.
     *
     * @param registerUserRequest the request object containing registration details
     * @throws PasswordMisMatchException if the password and confirmation-password do not match
     * @throws NotUniqueException        if the email or username is already in use
     */
    public void registerUser(RegisterUserRequest registerUserRequest) {
        log.info("Trying to register new user with email={}", registerUserRequest.email());
        validateCredentials(registerUserRequest);

        String hashedPw = passwordEncoder.encode(registerUserRequest.password());
        User user = userMapper.toUser(registerUserRequest, hashedPw, Role.MEMBER);
        userRepository.save(user);

        log.info("New user registered with id={} and email={}", user.getUserId(), user.getEmail());

    }

    /**
     * Retrieves the authenticated user
     *
     * @param userId ID of aiuthenticated user
     * @return {@link MyDTO} A detailed view of the authenticated user
     */
    public MyDTO getAuthenticatedUser(Long userId) {
        User user = getUserOrThrow(userId);
        return userMapper.toMyDTO(user);
    }

    /**
     * Updates an existing user's information.
     *
     * @param userRequest the request object containing updated user details
     * @param userId      the ID of the user to update
     * @return the updated user details as an {@link UpdateUserResponse}
     * @throws NotFoundException if the user with the given ID does not exist
     */
    public UpdateUserResponse updateUser(UpdateUserRequest userRequest, Long userId) {
        log.info("Trying to update user with id={}", userId);

        User user = getUserOrThrow(userId);
        userMapper.updateUser(userRequest, user);
        userRepository.save(user);

        log.info("User with id={} updated", userId);
        return userMapper.toUpdateUserResponse(user);


    }

    /**
     * Retrieves a list of all users
     *
     * @return a list of {@link UserListDTO} objects
     */
    public List<UserListDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserListDTO).toList();

    }


    /**
     * Finds a specific user by their ID and returns standard user data.
     *
     * @param userId the ID of the user to find
     * @return the user details as a {@link UserDTO}
     * @throws NotFoundException if the user does not exist
     */
    public UserDTO getUser(Long userId) {
        return userRepository.findById(userId)
                .map(userMapper::toUserDTO).orElseThrow(NotFoundException::new);
    }


    /**
     * Deletes a user from the system by their ID.
     *
     * @param userId the ID of the user to delete
     * @throws NotFoundException if the user does not exist
     */
    public void deleteUser(Long userId) {
        log.info("Trying to delete user with id={}", userId);
        User userToDelete = getUserOrThrow(userId);
        userRepository.delete(userToDelete);
        log.info("User with id={} removed", userId);
    }

    /* ************************** ADMIN METHODS *****************************'  */


    /**
     * Retrieves a list of all Users with more detailed information for Admin user
     *
     * @param pageable Paginates list
     * @return {@link AdminUserDTO} a detailed list of users
     */
    public Page<AdminUserDTO> getAllUsersAdmin(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toAdminUserDTO);
    }

    /**
     * Updates and saves the Role for a user. Intended for Admin use.
     *
     * @param request {@link RoleRequest} contains the email of the user and the new role type
     * @return {@link RoleResponse} containing the updated role information
     * @throws NotFoundException if the user with the specified email is not found
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

    /**
     * Performs an administrative update of a user's details.
     *
     * @param userRequest the request object containing updated user details for admin
     * @param userId      the ID of the user to update
     * @return the updated user data as an {@link UpdateUserResponse}
     * @throws NotFoundException if the user does not exist
     */
    public UpdateUserResponse updateUserAdmin(UpdateUserRequest userRequest,
                                              Long userId) {
        log.info("Admin update initiated for user with id={}", userId);
        User user = getUserOrThrow(userId);
        userMapper.updateUser(userRequest, user);
        userRepository.save(user);

        return userMapper.toUpdateUserResponse(user);

    }

    /* ********************** HELP METHODS *********************** */


    /**
     * Validates that the registration details are unique and consistent.
     * Checks if passwords match and if the email/username is already registered.
     *
     * @param registerUserRequest the request object to validate
     * @throws PasswordMisMatchException if passwords do not match
     * @throws NotUniqueException        if email or username is already taken
     */

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


    /**
     * Retrieves a user and throws an exception if not found
     *
     * @param userId ID of user to fetch
     * @return User entity
     */
    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with id={} not found", userId);
                    return new NotFoundException("User with id " + userId + " not found");
                });
    }


}
