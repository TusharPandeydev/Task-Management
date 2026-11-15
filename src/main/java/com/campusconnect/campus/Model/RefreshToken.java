package com.campusconnect.campus.Model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.cglib.core.Local;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Data
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // WHY NOT .AUTO
    private Long id;

    @Column(nullable=false,unique=true)
    private String tokenHash;
    @Column(nullable=false)
    private LocalDateTime expiresAt;
    @Column(nullable=false)
    private boolean used;
    @Column(nullable=false)
    private boolean revoked;
    @Column(nullable=false)
    private LocalDateTime createdAt;
    @Column(nullable=false)
    // don't try to set unique true here because multiple refresh token can have same family ID
    private String familyId;
    @ManyToOne
    @JoinColumn(name="User_ID")
    private User user;
}
