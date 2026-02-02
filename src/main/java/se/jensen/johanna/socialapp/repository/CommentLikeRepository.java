package se.jensen.johanna.socialapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import se.jensen.johanna.socialapp.model.CommentLike;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    @Query("SELECT COUNT(cl)>0 FROM CommentLike cl WHERE cl.comment.commentId=:commentId AND cl.user.userId=:userId")
    Boolean existsBetween(Long commentId, Long userId);

    void deleteCommentLikeByComment_CommentIdAndUser_UserId(Long commentId, Long userId);

    Integer countByComment_CommentId(Long commentId);
}
