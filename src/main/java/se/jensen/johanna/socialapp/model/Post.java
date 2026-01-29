package se.jensen.johanna.socialapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a post in the social-app.
 */
@Getter
@Setter
@Entity
@Table(name = "posts")
@NoArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @NotNull
    private String text;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;


    private LocalDateTime updatedAt;

    /**
     * Represents the author of the post
     */
    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    /**
     * Represents the comments on the post.
     * If a post is deleted, so is the comments because they live within a post
     */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<Comment> comments = new ArrayList<>();


}
