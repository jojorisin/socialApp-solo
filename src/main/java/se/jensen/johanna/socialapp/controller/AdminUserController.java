package se.jensen.johanna.socialapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.jensen.johanna.socialapp.dto.admin.*;
import se.jensen.johanna.socialapp.service.UserService;

import java.util.List;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<AdminUserDTO>> getAllUsersAdmin() {
        List<AdminUserDTO> userDTOS = userService.findAllUsersAdmin();

        return ResponseEntity.ok(userDTOS);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserDTO> getUserAdmin(@PathVariable Long userId) {
        AdminUserDTO userDTO = userService.findUserAdmin(userId);

        return ResponseEntity.ok(userDTO);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/roles")
    public ResponseEntity<RoleResponse> giveRole(@Valid @RequestBody RoleRequest roleRequest) {
        RoleResponse roleResponse = userService.addRole(roleRequest);

        return ResponseEntity.ok(roleResponse);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<AdminUpdateUserResponse> updateUserAdmin(
            @PathVariable Long userId,
            @RequestBody AdminUpdateUserRequest userRequest) {
        AdminUpdateUserResponse userResponse = userService.updateUserAdmin(
                userRequest, userId);

        return ResponseEntity.ok(userResponse);
    }
}
