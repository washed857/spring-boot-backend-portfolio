package rs.nms.newsroom.server.config.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import rs.nms.newsroom.server.domain.Permission;
import rs.nms.newsroom.server.domain.User;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom implementation of {@link UserDetails} for Spring Security.
 * Wraps the application's {@link User} entity and exposes necessary authentication details.
 */
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final Long clientId;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Constructs CustomUserDetails based on the provided {@link User} entity.
     *
     * @param user the domain user entity
     */
    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.clientId = user.getClient() != null ? user.getClient().getId() : null;
        this.username = user.getUsername();
        this.password = user.getPasswordHash();
        this.authorities = extractAuthorities(user);
    }

    /**
     * Extracts granted authorities from the user's assigned permissions.
     *
     * @param user the domain user entity
     * @return a collection of granted authorities
     */
    private Collection<? extends GrantedAuthority> extractAuthorities(User user) {
        Set<Permission> permissions = user.getRole().getPermissions();
        return permissions.stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                .collect(Collectors.toSet());
    }

    /**
     * Returns the unique ID of the user.
     * @return user ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the client ID associated with the user, if any.
     * @return client ID or null if not assigned
     */
    public Long getClientId() {
        return clientId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}