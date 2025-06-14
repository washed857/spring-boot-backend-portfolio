package rs.nms.newsroom.server.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import rs.nms.newsroom.server.domain.enums.StoryStatus;

import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a newsroom story, including metadata, workflow status,
 * author, rundown association, story items, and audit fields.
 * <p>
 * Supports approval, locking, comments, and relationship to story type and rundown.
 * </p>
 */
@Entity
@Table(name = "story", uniqueConstraints = {
        @UniqueConstraint(columnNames = "external_id")
})
@Getter
@Setter
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Optional external system identifier.
     */
    @Column(name = "external_id", length = 100, unique = true)
    private String externalId;

    @Column(nullable = false, length = 255)
    private String title;

    /**
     * Current status in the editorial workflow.
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private StoryStatus status;

    /**
     * The user who authored the story.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /**
     * Rundown (production playlist) association.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rundown_id")
    private Rundown rundown;

    /**
     * The type/category of the story.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_type_id")
    private StoryType storyType;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Multitenancy: the client (organization) this story belongs to.
     */
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StoryItem> storyItems = new HashSet<>();

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    /**
     * If locked, which user currently holds the lock.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locked_by_id")
    private User lockedBy;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @Column(name = "story_order")
    private Integer order;

    /**
     * Adds a story item and maintains the bidirectional relationship.
     */
    public void addStoryItem(StoryItem item) {
        storyItems.add(item);
        item.setStory(this);
    }

    /**
     * Removes a story item and maintains the bidirectional relationship.
     */
    public void removeStoryItem(StoryItem item) {
        storyItems.remove(item);
        item.setStory(null);
    }
}