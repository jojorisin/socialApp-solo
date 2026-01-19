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

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final UserRepository userRepository;


    //Returnerar lista ordnad med createdAt desc (senaste först)
    public Page<PostResponseDTO> findAllPosts(Pageable pageable) {
        Page<Post> postPage = postRepository.findAll(pageable);

        return postPage.map(postMapper::toPostResponseDTO);

    }

    public Page<PostResponseDTO> findAllPostsForUser(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }
        Page<Post> userPostPage = postRepository.findByUser_UserId(userId, pageable);
        return userPostPage.map(postMapper::toPostResponseDTO);
    }

    public List<PostResponse> getPostsForUser(Long userId){
        List<Post> userPosts=postRepository.findAllPostsByUserId(userId);
        return userPosts.stream()
                .map(postMapper::toPostResponse)
                .toList();
    }
    public PostResponseDTO findPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(NotFoundException::new);

        return postMapper.toPostResponseDTO(post);
    }

    public PostResponseDTO addPost(PostRequest postRequest, Long userId) {
        log.info("Trying to add post for user with id={}", userId);
        User user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
        Post post = postMapper.toPost(postRequest);
        post.setUser(user);
        postRepository.save(post);
        log.info("Post created for user with id={}", userId);
        return postMapper.toPostResponseDTO(post);

    }

    public UpdatePostResponseDTO updatePost(PostRequest postRequest, Long postId, Long userId) {
        log.info("Trying to update post with id={} for user with id={}",postId, userId);
        Post post = postRepository.findById(postId).orElseThrow(() -> {
            log.warn("Post with id={} not found when trying to update by user with id={}",postId, userId);
            return new NotFoundException();
        });
        if (!post.getUser().getUserId().equals(userId)) {
            log.warn("User with id={} attempted to modify post with id={} without permission", userId, postId);
            throw new ForbiddenException("You are not authorized to edit this post");
        }
        postMapper.updatePost(postRequest, post);
        postRepository.save(post);

        log.info("Post with id={} is updated for user with id={}",postId, userId);
        return postMapper.toUpdatePostResponseDTO(post);

    }

    public void deletePost(Long postId, Long userId) {
        log.info("Trying to delete post with id={} for user with id={}",postId, userId);
        Post post = postRepository.findById(postId).orElseThrow(() -> {
            log.warn("Post with id={} not found when trying to delete by user with id={}",postId, userId);
           return new NotFoundException();
        });
        if (!post.getUser().getUserId().equals(userId)) {
            log.warn("User with id={} attempted to delete post with id={} without permission", userId, postId);
            throw new ForbiddenException("You are not authorized to delete this post");
        }
        postRepository.delete(post);
        log.info("Post with id={} deleted for user with id={}",postId, userId);
    }

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

    public void deletePostAdmin(Long postId) {
        log.info("ADMIN trying to delete post with id={}", postId);
        Post post = postRepository.findById(postId).orElseThrow(() -> {
            log.warn("ADMIN was unable to delete post with id={}", postId);
            return new NotFoundException();
        });

        postRepository.delete(post);
        log.info("ADMIN successfully deleted post with id={}", postId);
    }
}
