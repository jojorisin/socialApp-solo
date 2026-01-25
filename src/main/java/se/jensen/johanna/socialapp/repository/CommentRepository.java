package se.jensen.johanna.socialapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.jensen.johanna.socialapp.model.Comment;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.replies " +
            "WHERE c.post.postId=:postId AND c.parent IS NULL " +
            "ORDER BY c.createdAt DESC")
    List<Comment> findAllMainCommentsWithReplies(@Param("postId")
                                                 Long postId);

    /**
     * Retrieves all main comments for a specific post
     *
     * @param postId ID of post
     * @return List of main comments in descending order
     */
    @Query("SELECT c FROM Comment c WHERE c.post.postId=:postId AND c.parent IS NULL " +
            "ORDER BY c.createdAt DESC")
    List<Comment> findAllMainComments(@Param("postId") Long postId);

    /**
     * Retrieves all replies for a specific comment
     *
     * @param parentId ID of the parent-comment
     * @return List of child-comments in ascending order
     */
    Page<Comment> findByParent_CommentIdOrderByCreatedAtAsc(Long parentId, Pageable pageable);

    Page<Comment> findByPost_postIdAndParentIsNull(Long postId, Pageable pageable);
}
