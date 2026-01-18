package se.jensen.johanna.socialapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import se.jensen.johanna.socialapp.model.Post;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p WHERE p.user.userId = :userId " +
            "ORDER BY p.createdAt DESC")
    List<Post> findAllPostsByUserId(Long userId);
    Page<Post> findByUser_UserId(Long userId, Pageable pageable);
}