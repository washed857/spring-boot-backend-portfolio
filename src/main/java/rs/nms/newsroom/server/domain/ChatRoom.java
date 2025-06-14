package rs.nms.newsroom.server.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a chat room in the newsroom system.
 * <p>
 * Supports multiple participants, stores messages, and contains metadata such as room type and creator.
 * </p>
 */
@Entity
@Table(name = "chat_room")
@Getter
@Setter
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name of the chat room.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * The type of the chat room (e.g. PUBLIC, PRIVATE, STORY).
     */
    @Column(nullable = false, length = 50)
    private String type; // PUBLIC, PRIVATE, STORY

    /**
     * The user who created the chat room.
     */
    @Column(name = "created_by", length = 100)
    private String createdBy;

    /**
     * The timestamp when the chat room was created.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * All messages posted in this chat room.
     */
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ChatMessage> messages = new HashSet<>();

    /**
     * All participants of this chat room.
     */
    @ManyToMany
    @JoinTable(
        name = "chat_room_user",
        joinColumns = @JoinColumn(name = "chat_room_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

    // Helper methods
    public void addParticipant(User user) {
        participants.add(user);
        user.getChatRooms().add(this);
    }

    public void removeParticipant(User user) {
        participants.remove(user);
        user.getChatRooms().remove(this);
    }
}