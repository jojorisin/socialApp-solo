package se.jensen.johanna.socialapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import se.jensen.johanna.socialapp.model.User;
import se.jensen.johanna.socialapp.repository.UserRepository;
import se.jensen.johanna.socialapp.security.MyUserDetails;

/**
 * Custom implementation of the Spring Security {@link UserDetailsService} interface.
 * <p>
 * This service is responsible for retrieving user authentication and authorization data
 * from the database during the login process. It bridges the application's domain
 * {@link User} entity and Spring Security's requirement for a {@link UserDetails} object.
 */

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    /**
     * Locates the user based on the provided username.
     * <p>
     * This method is invoked by the Spring Security authentication provider. It searches
     * for the user in the database via the {@link UserRepository}. If found, the user data
     * is wrapped in a {@link MyUserDetails} instance.
     *
     * @param username the username identifying the user whose data is required.
     * @return a fully populated {@link UserDetails} object for the specified user.
     * @throws UsernameNotFoundException if no user with the given username exists in the database.
     */

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found " + username));
        System.out.println("DEBUG: Försöker logga in användare: " + username);
        System.out.println("DEBUG: Lösenord i Java-objektet: " + user.getPassword());
        return new MyUserDetails(user);

    }
}
