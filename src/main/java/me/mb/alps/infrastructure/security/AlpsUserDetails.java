package me.mb.alps.infrastructure.security;

import me.mb.alps.domain.enums.UserRole;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public record AlpsUserDetails(
        String userId,
        String username,
        String password,
        List<String> roles
) implements UserDetails {

    @NonNull
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority(
                        r.startsWith("ROLE_")
                            ? r
                            : "ROLE_" + r
                        )
                )
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @NonNull
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

    /** Mỗi account 1 role. */
    public UserRole getRole() {
        if (roles == null || roles.isEmpty()) {
            return UserRole.CUSTOMER;
        }
        return UserRole.valueOf(roles.getFirst().startsWith("ROLE_")
                ? roles.getFirst().substring(5)
                : roles.getFirst());
    }
}
