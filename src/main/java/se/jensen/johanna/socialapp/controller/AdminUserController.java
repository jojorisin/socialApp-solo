package se.jensen.johanna.socialapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.jensen.johanna.socialapp.dto.admin.*;
import se.jensen.johanna.socialapp.service.UserService;

import java.util.List;

/**
 * REST controller for administrative user management operations.
 * All endpoints in this controller require the user to have the 'ADMIN' role.
 */

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;

    /**
     * Retrieves a list of all users in the system with administrative details.
     *
     * @return a {@link ResponseEntity} containing a list of {@link AdminUserDTO} objects.
     */
    @GetMapping
    public ResponseEntity<List<AdminUserDTO>> getAllUsersAdmin() {
        List<AdminUserDTO> userDTOS = userService.findAllUsersAdmin();

        return ResponseEntity.ok(userDTOS);
    }

    /**
     * Retrieves detailed information for a specific user.
     *
     * @param userId the ID of the user to retrieve.
     * @return a {@link ResponseEntity} containing the {@link AdminUserDTO} of the specified user.
     */

    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserDTO> getUserAdmin(@PathVariable Long userId) {
        AdminUserDTO userDTO = userService.findUserAdmin(userId);

        return ResponseEntity.ok(userDTO);
    }

    /**
     * Deletes a user from the system.
     *
     * @param userId the ID of the user to delete.
     * @return a {@link ResponseEntity} with 204 No Content status on success.
     */

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Grants or updates roles for a user.
     *
     * @param roleRequest the request object containing role assignment details.
     * @return a {@link ResponseEntity} containing the {@link RoleResponse}.
     */

    @PatchMapping("/roles")
    public ResponseEntity<RoleResponse> addRole(@Valid @RequestBody RoleRequest roleRequest) {
        RoleResponse roleResponse = userService.addRole(roleRequest);

        return ResponseEntity.ok(roleResponse);
    }

    /**
     * Updates user information from an administrative perspective.
     *
     * @param userId      the ID of the user to update.
     * @param userRequest the request object containing updated user information.
     * @return a {@link ResponseEntity} containing the {@link AdminUpdateUserResponse}.
     */
    @PatchMapping("/{userId}")
    public ResponseEntity<AdminUpdateUserResponse> updateUserAdmin(
            @PathVariable Long userId,
            @RequestBody AdminUpdateUserRequest userRequest) {
        AdminUpdateUserResponse userResponse = userService.updateUserAdmin(
                userRequest, userId);

        return ResponseEntity.ok(userResponse);
    }
}
