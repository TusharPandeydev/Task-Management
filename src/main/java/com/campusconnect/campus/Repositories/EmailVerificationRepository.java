package com.campusconnect.campus.Repositories;

import com.campusconnect.campus.Model.EmailVerification;
import com.campusconnect.campus.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    public Optional<EmailVerification> findByUser(User user);

    Optional<EmailVerification> findByUserId(Long id);
}
