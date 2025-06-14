package rs.nms.newsroom.server.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a chat message sent by a user within a chat room.
 * <p>
 * Stores the message content, sender, associated room, and timestamp.
 * </p>
 */
@Entity
@Table(name = "chat_message")
@Getter
@Setter
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The chat room to which this message belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    /**
     * The user who sent the message.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * The message content.
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    /**
     * The timestamp when the message was created.
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime timestamp;
}