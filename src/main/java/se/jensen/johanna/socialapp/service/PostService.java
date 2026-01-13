package se.jensen.johanna.socialapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.jensen.johanna.socialapp.dto.PostDTO;
import se.jensen.johanna.socialapp.dto.PostListDTO;
import se.jensen.johanna.socialapp.dto.PostRequest;
import se.jensen.johanna.socialapp.dto.PostResponse;
import se.jensen.johanna.socialapp.dto.admin.AdminUpdatePostRequest;
import se.jensen.johanna.socialapp.dto.admin.AdminUpdatePostResponse;
import se.jensen.johanna.socialapp.exception.ForbiddenException;
import se.jensen.johanna.socialapp.exception.NotFoundException;
import se.jensen.johanna.socialapp.mapper.CommentMapper;
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
    private final CommentMapper commentMapper;


    public List<PostListDTO> findAllPosts() {
        return postRepository.findAll().stream()
                .map(postMapper::toPostList).toList();

    }

    public PostDTO findPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(NotFoundException::new);

        return postMapper.toDTO(post, commentMapper);
    }

    public PostResponse addPost(PostRequest postRequest, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(NotFoundException::new);
        Post post = postMapper.toPost(postRequest);
        post.setUser(user);
        postRepository.save(post);
        return postMapper.toPostResponse(post);

    }

    public PostResponse updatePost(PostRequest postRequest, Long postId, String username) {
        Post post = postRepository.findById(postId).orElseThrow(NotFoundException::new);
        if (!post.getUser().getUsername().equals(username)) {
            throw new ForbiddenException("You are not authorized to edit this post");
        }
        postMapper.updatePost(postRequest, post);
        postRepository.save(post);


        return postMapper.toPostResponse(post);

    }

    public void deletePost(Long postId, String username) {
        Post post = postRepository.findById(postId).orElseThrow(NotFoundException::new);
        if (!post.getUser().getUsername().equals(username)) {
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
