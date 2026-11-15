package com.campusconnect.campus.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Entity
@Data
public class EmailVerification {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private String otp;
    private LocalDateTime expireAt;

    @OneToOne
    @JoinColumn(name="user_id")
    private User user;
}
