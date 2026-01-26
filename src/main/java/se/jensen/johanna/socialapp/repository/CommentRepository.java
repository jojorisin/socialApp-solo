package se.jensen.johanna.socialapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.jensen.johanna.socialapp.model.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {


    /**
     * Retrieves all replies for a specific comment
     *
     * @param parentId ID of the parent-comment
     * @return List of paginated child-comments in ascending order
     */
    Page<Comment> findByParent_CommentIdOrderByCreatedAtAsc(Long parentId, Pageable pageable);

    /**
     * Retrieves all main comments without a parent-id for a specific post
     *
     * @param postId   id of post to fetch comments for
     * @param pageable Returns paginated list
     * @return {@link Comment}
     */
    Page<Comment> findByPost_postIdAndParentIsNull(Long postId, Pageable pageable);
}
