package se.jensen.johanna.socialapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.jensen.johanna.socialapp.model.Role;
import se.jensen.johanna.socialapp.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.role=:role")
    List<User> findAllUsersByRole(@Param("role") Role role);

    @Query("SELECT u FROM User u WHERE u.role='MEMBER' AND u.userId=:userId")
    Optional<User> findUserMemberRole(@Param("userId") Long userId);
}
