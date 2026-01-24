package se.jensen.johanna.socialapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import se.jensen.johanna.socialapp.dto.PostRequest;
import se.jensen.johanna.socialapp.dto.PostResponse;
import se.jensen.johanna.socialapp.dto.PostResponseDTO;
import se.jensen.johanna.socialapp.dto.UpdatePostResponseDTO;
import se.jensen.johanna.socialapp.dto.admin.AdminUpdatePostRequest;
import se.jensen.johanna.socialapp.dto.admin.AdminUpdatePostResponse;
import se.jensen.johanna.socialapp.exception.ForbiddenException;
import se.jensen.johanna.socialapp.exception.NotFoundException;
import se.jensen.johanna.socialapp.mapper.PostMapper;
import se.jensen.johanna.socialapp.model.Post;
import se.jensen.johanna.socialapp.model.User;
import se.jensen.johanna.socialapp.repository.PostRepository;
import se.jensen.johanna.socialapp.repository.UserRepository;

import java.util.List;

/**
 * Service class for managing posts in the social application.
 * Handles business logic for post creation, retrieval, updates, and deletion,
 * including administrative actions and authorization checks.
 */

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final UserRepository userRepository;


    /**
     * Retrieves all posts in a paginated format, typically ordered by creation date.
     *
     * @param pageable the pagination and sorting information
     * @return a {@link Page} of {@link PostResponseDTO} containing post details
     */
    public Page<PostResponseDTO> findAllPosts(Pageable pageable) {
        Page<Post> postPage = postRepository.findAll(pageable);

        return postPage.map(postMapper::toPostResponseDTO);

    }

    /**
     * Retrieves all posts belonging to a specific user in a paginated format.
     *
     * @param userId   the ID of the user whose posts are to be retrieved
     * @param pageable the pagination and sorting information
     * @return a {@link Page} of {@link PostResponseDTO} for the specified user
     * @throws NotFoundException if the user with the specified ID does not exist
     */
    public Page<PostResponseDTO> findAllPostsForUser(Long userId, Pageable pageable) {
        getUserOrThrow(userId);
        Page<Post> userPostPage = postRepository.findByUser_UserId(userId, pageable);
        return userPostPage.map(postMapper::toPostResponseDTO);
    }

    /**
     * Retrieves a list of all posts for the current logged-in user.
     * Response contains less user information than findAllPostsForUser().
     *
     * @param userId the ID of the user whose posts are to be retrieved
     * @return a {@link List} of {@link PostResponse}
     */
    public List<PostResponse> getPostsForCurrentUser(Long userId) {
        List<Post> userPosts = postRepository.findAllPostsByUserId(userId);
        return userPosts.stream()
                .map(postMapper::toPostResponse)
                .toList();
    }

    /**
     * Finds a single post by its unique identifier.
     *
     * @param postId the ID of the post to retrieve
     * @return the {@link PostResponseDTO} representing the found post
     * @throws NotFoundException if the post with the specified ID is not found
     */
    public PostResponseDTO findPost(Long postId) {
        Post post = getPostOrThrow(postId);
        return postMapper.toPostResponseDTO(post);
    }

    /**
     * Creates and saves a new post for a specific user.
     *
     * @param postRequest the data containing the post content
     * @param userId      the ID of the user creating the post
     * @return the {@link PostResponseDTO} representing the newly created post
     * @throws NotFoundException if the user with the specified ID is not found
     */

    public PostResponseDTO addPost(PostRequest postRequest, Long userId) {
        log.info("Trying to add post for user with id={}", userId);
        User user = getUserOrThrow(userId);
        Post post = postMapper.toPost(postRequest);
        post.setUser(user);
        postRepository.save(post);
        log.info("Post created for user with id={}", userId);
        return postMapper.toPostResponseDTO(post);

    }

    /**
     * Updates an existing post. Verifies that the user attempting the update is the owner.
     *
     * @param postRequest the updated content of the post
     * @param postId      the ID of the post to update
     * @param userId      the ID of the user requesting the update
     * @return the {@link UpdatePostResponseDTO} representing the updated post
     * @throws NotFoundException  if the post with the specified ID is not found
     * @throws ForbiddenException if the user is not authorized to edit the post
     */
    public UpdatePostResponseDTO updatePost(PostRequest postRequest, Long postId, Long userId) {
        log.info("Trying to update post with id={} for user with id={}", postId, userId);
        Post post = getPostOrThrow(postId);
        validateAuthor(post, userId);
        postMapper.updatePost(postRequest, post);
        postRepository.save(post);

        log.info("Post with id={} is updated for user with id={}", postId, userId);
        return postMapper.toUpdatePostResponseDTO(post);

    }

    /**
     * Deletes an existing post. Verifies that the user attempting the deletion is the owner.
     *
     * @param postId the ID of the post to delete
     * @param userId the ID of the user requesting the deletion
     * @throws NotFoundException  if the post with the specified ID is not found
     * @throws ForbiddenException if the user is not authorized to delete the post
     */
    public void deletePost(Long postId, Long userId) {
        log.info("Trying to delete post with id={} for user with id={}", postId, userId);
        Post post = getPostOrThrow(postId);

        validateAuthor(post, userId);

        postRepository.delete(post);
        log.info("Post with id={} deleted for user with id={}", postId, userId);
    }

    /* *******************    METHODS FOR ADMIN   ******************* */

    /**
     * Administrative method to update any post regardless of ownership.
     *
     * @param adminRequest the updated post data for administration
     * @param postId       the ID of the post to update
     * @return the {@link AdminUpdatePostResponse} representing the updated post
     * @throws NotFoundException if the post with the specified ID is not found
     */
    public AdminUpdatePostResponse updatePostAdmin(
            AdminUpdatePostRequest adminRequest, Long postId) {
        log.info("ADMIN trying to update post with id={}", postId);
        Post post = postRepository.findById(postId).orElseThrow(() -> {
            log.warn("ADMIN was unable to update post with id={}", postId);
            return new NotFoundException();
        });

        postMapper.updatePostAdmin(adminRequest, post);
        postRepository.save(post);
        log.info("ADMIN successfully updated post with id={}", postId);

        return postMapper.toAdminUpdateResponse(post);
    }

    /**
     * Administrative method to delete any post regardless of ownership.
     *
     * @param postId the ID of the post to delete
     * @throws NotFoundException if the post with the specified ID is not found
     */
    public void deletePostAdmin(Long postId) {
        log.info("ADMIN trying to delete post with id={}", postId);
        Post post = postRepository.findById(postId).orElseThrow(() -> {
            log.warn("ADMIN was unable to delete post with id={}", postId);
            return new NotFoundException();
        });

        postRepository.delete(post);
        log.info("ADMIN successfully deleted post with id={}", postId);
    }


    private Post getPostOrThrow(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("Post with id={} not found", postId);
                    return new NotFoundException("Post with id " + postId + " not found.");

                });
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with id={} not found", userId);
                    return new NotFoundException("User with id " + userId + " not found.");
                });
    }

    private void validateAuthor(Post post, Long userId) {
        if (!post.getUser().getUserId().equals(userId)) {
            log.warn("User with id={} attempted to modify post with id={} without permission", userId, post.getPostId());
            throw new ForbiddenException("You are not authorized to modify this post.");
        }
    }
}
