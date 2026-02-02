package se.jensen.johanna.socialapp.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import se.jensen.johanna.socialapp.model.PostLike;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    @Query("SELECT COUNT (pl)>0 FROM PostLike pl WHERE pl.post.postId=:postId AND pl.user.userId=:userId")
    boolean existsBetween(Long postId, Long userId);

    Integer countByPost_PostId(Long postId);

    void deleteByPost_PostIdAndUser_UserId(Long postId, Long userId);
}
