package com.campusconnect.campus.Seeder;

import com.campusconnect.campus.Enum.Role;
import com.campusconnect.campus.Model.User;
import com.campusconnect.campus.Repositories.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminName;
    private final String adminPassword;
    private final String adminEmail;

    public AdminSeeder(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${app.admin.name}") String adminName,
                       @Value("${app.admin.email}")String adminEmail,
                       @Value("${app.admin.password}")String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminName = adminName;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;

    }
    @Override
    public void run(String @NonNull ... args) throws Exception {
        boolean adminFound=userRepository.existsByRoleAndEmailVerifiedTrue(Role.ROLE_ADMIN);
        if(!adminFound){
            User user=new User();
            user.setRole(Role.ROLE_ADMIN);
            user.setEmail(adminEmail);
            user.setPassword(passwordEncoder.encode(adminPassword));
            user.setFullName(adminName);
            user.setEmailVerified(true);
            user.setPhoneNumber("9898978654");
            User savedUser=userRepository.save(user);
            LocalDateTime time=savedUser.getCreatedAt();
            System.out.println(">>> User " + savedUser.getFullName() + " has been initialized with ADMIN " +
                    "role at " + time);
        }

    }
}
