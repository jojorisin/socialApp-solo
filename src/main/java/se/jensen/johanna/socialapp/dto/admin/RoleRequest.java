package se.jensen.johanna.socialapp.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import se.jensen.johanna.socialapp.model.Role;

/**
 * Admin request for updating a role on a user
 *
 * @param email email of user to give a role
 * @param role  type of role
 */
public record RoleRequest(
        @Email
        @NotBlank String email,
        @NotNull(message = "Please add a role.")
        Role role
) {
}
