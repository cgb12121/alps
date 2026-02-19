package me.mb.alps.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.mb.alps.domain.enums.UserRole;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Column(unique = true, nullable = false, length = 128)
    private String username;

    @Column(length = 255)
    private String displayName;

    @Column(length = 128)
    private String email;

    /** BCrypt hash; nullable cho user cũ (dev-default-password). */
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole role = UserRole.CUSTOMER;

    /** Optional: map account (CUSTOMER) tới 1 customer. */
    @Column(name = "customer_id")
    private UUID customerId;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
