package se.jensen.johanna.socialapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.jensen.johanna.socialapp.model.Post;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p WHERE p.user.userId=:userID")
    List<Post> findAllPostsByUserId(@Param("userId") Long userId);

    List<Post> findAllByOrderByCreatedAtDesc();

}