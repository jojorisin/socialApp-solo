package se.jensen.johanna.socialapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import se.jensen.johanna.socialapp.dto.*;
import se.jensen.johanna.socialapp.exception.ForbiddenException;
import se.jensen.johanna.socialapp.exception.NotFoundException;
import se.jensen.johanna.socialapp.mapper.PostMapper;
import se.jensen.johanna.socialapp.model.Post;
import se.jensen.johanna.socialapp.model.User;
import se.jensen.johanna.socialapp.repository.PostRepository;
import se.jensen.johanna.socialapp.service.helper.EntityProvider;

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
    private final EntityProvider entityProvider;


    /**
     * Retrieves all posts in a paginated format, typically ordered by creation date.
     *
     * @param pageable the pagination and sorting information
     * @return a {@link Page} of {@link PostDTO} containing post and author-details
     */
    public Page<PostDTO> getAllPosts(Pageable pageable) {
        Page<Post> postPage = postRepository.findAll(pageable);

        return postPage.map(postMapper::toPostDTO);

    }

    /**
     * Retrieves all posts belonging to a specific user in a paginated format.
     *
     * @param userId   the ID of the user whose posts are to be retrieved
     * @param pageable the pagination and sorting information
     * @return a {@link Page} of {@link UserPostDTO} containing post-details
     * @throws NotFoundException if the user with the specified ID does not exist
     */
    public Page<UserPostDTO> getPostsForUser(Long userId, Pageable pageable) {
        entityProvider.getUserOrThrow(userId);
        Page<Post> userPosts = postRepository.findByUser_UserId(userId, pageable);
        return userPosts.map(postMapper::toUserPostDTO);
    }


    /**
     * Finds a single post by its unique identifier.
     *
     * @param postId the ID of the post to retrieve
     * @return the {@link PostDTO} representing the found post
     * @throws NotFoundException if the post with the specified ID is not found
     */
    public PostDTO getPost(Long postId) {
        Post post = entityProvider.getPostOrThrow(postId);
        return postMapper.toPostDTO(post);
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
        User user = entityProvider.getUserOrThrow(userId);
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
     * @return the {@link UpdatePostResponse} representing the updated post
     * @throws NotFoundException  if the post with the specified ID is not found
     * @throws ForbiddenException if the user is not authorized to edit the post
     */
    public UpdatePostResponse updatePost(PostRequest postRequest, Long postId, Long userId) {
        log.info("Trying to update post with id={} for user with id={}", postId, userId);
        Post post = entityProvider.getPostOrThrow(postId);
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
        Post post = entityProvider.getPostOrThrow(postId);

        validateAuthor(post, userId);

        postRepository.delete(post);
        log.info("Post with id={} deleted for user with id={}", postId, userId);
    }

    /* *******************    METHODS FOR ADMIN   ******************* */

    /**
     * Administrative method to update any post regardless of ownership.
     *
     * @param postRequest the updated post-data for administration
     * @param postId      the ID of the post to update
     * @return the {@link UpdatePostResponse} representing the updated post
     * @throws NotFoundException if the post with the specified ID is not found
     */
    public UpdatePostResponse updatePostAdmin(
            PostRequest postRequest, Long postId) {
        log.info("ADMIN trying to update post with id={}", postId);
        Post post = entityProvider.getPostOrThrow(postId);

        postMapper.updatePost(postRequest, post);
        postRepository.save(post);
        log.info("ADMIN successfully updated post with id={}", postId);

        return postMapper.toUpdatePostResponseDTO(post);
    }

    /**
     * Administrative method to delete any post regardless of ownership.
     *
     * @param postId the ID of the post to delete
     * @throws NotFoundException if the post with the specified ID is not found
     */
    public void deletePostAdmin(Long postId) {
        log.info("ADMIN trying to delete post with id={}", postId);

        Post post = entityProvider.getPostOrThrow(postId);
        postRepository.delete(post);

        log.info("ADMIN successfully deleted post with id={}", postId);
    }


    private void validateAuthor(Post post, Long userId) {
        if (!post.getUser().getUserId().equals(userId)) {
            log.warn("User with id={} attempted to modify post with id={} without permission", userId, post.getPostId());
            throw new ForbiddenException("You are not authorized to modify this post.");
        }
    }
}
