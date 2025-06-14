package rs.nms.newsroom.server.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a user account in the newsroom system.
 * <p>
 * Supports role-based access, multitenancy, profile images, soft deletion,
 * and audit fields for account lifecycle management.
 * </p>
 */
@Entity
@Table(name = "user")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String fullName;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(length = 20)
    private String status = "ACTIVE";

    @Column(nullable = false)
    private boolean emailVerified = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToMany(mappedBy = "participants")
    private Set<ChatRoom> chatRooms = new HashSet<>();

    @Column(name = "profile_image_path", length = 512, nullable = false)
    private String profileImagePath = "/static/profile-default.png";

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status) && deletedAt == null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
