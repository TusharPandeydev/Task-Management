package com.campusconnect.campus.Repositories;

import com.campusconnect.campus.Enum.Role;
import com.campusconnect.campus.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository  extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    List<User> findByRoleAndEmailVerifiedTrue(Role role);
    boolean existsByRoleAndEmailVerifiedTrue(Role role);

    boolean existsByEmail(String email);
}
