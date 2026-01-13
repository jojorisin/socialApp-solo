package se.jensen.johanna.socialapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import se.jensen.johanna.socialapp.dto.*;
import se.jensen.johanna.socialapp.dto.admin.AdminUpdateUserRequest;
import se.jensen.johanna.socialapp.dto.admin.AdminUpdateUserResponse;
import se.jensen.johanna.socialapp.dto.admin.AdminUserDTO;
import se.jensen.johanna.socialapp.exception.NotFoundException;
import se.jensen.johanna.socialapp.exception.NotUniqueException;
import se.jensen.johanna.socialapp.exception.PasswordMisMatchException;
import se.jensen.johanna.socialapp.mapper.UserMapper;
import se.jensen.johanna.socialapp.model.Role;
import se.jensen.johanna.socialapp.model.User;
import se.jensen.johanna.socialapp.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    public RegisterUserResponse registerUser(UserRequest userRequest) {
        validateCredentials(userRequest);

        String hashedPw = passwordEncoder.encode(userRequest.password());
        User user = userMapper.toUser(userRequest, hashedPw, Role.MEMBER);
        userRepository.save(user);
        RegisterUserResponse response = new RegisterUserResponse();
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setUserId(user.getUserId());
        return response;
    }

    public UpdateUserResponse updateUser(UpdateUserRequest userRequest, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
        userMapper.updateUser(userRequest, user);
        userRepository.save(user);

        return userMapper.toUpdateUserResponse(user);

    }


    public List<UserListDTO> findAllUsers() {
        return userRepository.findAllUsersByRole(Role.MEMBER).stream()
                .map(userMapper::toUserListDTO).toList();

    }

    public UserWithPostsDTO getUserWithPosts(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
        return userMapper.toUserWithPosts(user);
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
        User userToDelete = userRepository.findById(userId).orElseThrow(NotFoundException::new);
        userRepository.delete(userToDelete);
    }

    public void deleteUserAdmin(Long userId) {
        User userToDelete = userRepository.findById(userId).orElseThrow(NotFoundException::new);
        userRepository.delete(userToDelete);
    }

    public RegisterUserResponse registerAdminUser(UserRequest userRequest) {
        validateCredentials(userRequest);

        String hashedPw = passwordEncoder.encode(userRequest.password());
        User user = userMapper.toUser(userRequest, hashedPw, Role.ADMIN);
        userRepository.save(user);
        RegisterUserResponse response = new RegisterUserResponse();
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setUserId(user.getUserId());
        return response;

    }

    public AdminUpdateUserResponse updateUserAdmin(AdminUpdateUserRequest userRequest,
                                                   Long userId) {
        User user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
        userMapper.updateUserAdmin(userRequest, user);
        userRepository.save(user);

        return userMapper.toAdminResponse(user);

    }

    public void validateCredentials(UserRequest userRequest) {
        if (!userRequest.password().equals(userRequest.confirmPassword())) {
            throw new PasswordMisMatchException();
        }
        if (userRepository.existsByEmail(userRequest.email())) {
            throw new NotUniqueException("Email is already registered. Log in or try different email.");
        }
        if (userRepository.existsByUsername(userRequest.username())) {
            throw new NotUniqueException("Username is already registered. Please choose a unique username.");
        }
    }


}
