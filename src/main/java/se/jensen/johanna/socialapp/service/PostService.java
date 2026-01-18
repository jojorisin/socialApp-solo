package se.jensen.johanna.socialapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
//    public List<PostResponse> getAllPostsByUser(Long userId){
//        User user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
//        List<Post> posts = postRepository.findByUser(user);
//    }
    public PostResponseDTO findPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(NotFoundException::new);

        return postMapper.toPostResponseDTO(post);
    }

    public PostResponseDTO addPost(PostRequest postRequest, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
        Post post = postMapper.toPost(postRequest);
        post.setUser(user);
        postRepository.save(post);
        return postMapper.toPostResponseDTO(post);

    }

    public UpdatePostResponseDTO updatePost(PostRequest postRequest, Long postId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(NotFoundException::new);
        if (!post.getUser().getUserId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to edit this post");
        }
        postMapper.updatePost(postRequest, post);
        postRepository.save(post);


        return postMapper.toUpdatePostResponseDTO(post);

    }

    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(NotFoundException::new);
        if (!post.getUser().getUserId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to delete this post");
        }
        postRepository.delete(post);

    }

    public AdminUpdatePostResponse updatePostAdmin(
            AdminUpdatePostRequest adminRequest, Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(NotFoundException::new);

        postMapper.updatePostAdmin(adminRequest, post);
        postRepository.save(post);

        return postMapper.toAdminUpdateResponse(post);
    }

    public void deletePostAdmin(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(NotFoundException::new);

        postRepository.delete(post);
    }
}
