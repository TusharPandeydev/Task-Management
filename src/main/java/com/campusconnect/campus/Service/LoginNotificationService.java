package com.campusconnect.campus.Service;

import com.campusconnect.campus.Model.User;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
@Service
public class LoginNotificationService {
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private static String sender;
    public LoginNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendLoginNotification(User user,LocalDateTime loginTime) {

        try {
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

            String formattedTime =
                    loginTime.format(formatter);

            SimpleMailMessage message = getSimpleMailMessage(user, formattedTime);

            mailSender.send(message);
            System.out.println(
                    ">>> SUCCESS: Login notification email sent to "
                            + user.getEmail()
            );

        } catch (Exception e) {
            System.err.println(
                    "Failed to send login notification email: "
                            + e.getMessage()
            );
        }
    }

    private static @NonNull SimpleMailMessage getSimpleMailMessage(User user, String formattedTime) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(user.getEmail());
        message.setSubject("TaskFlow Login Notification");
        message.setFrom(sender);
        message.setText(
                "Hello " + user.getFullName() + ",\n\n"
                        + "Your TaskFlow account was successfully logged in.\n\n"
                        + "Login time: " + formattedTime + "\n\n"
                        + "If this login was not performed by you, please secure your account.\n\n"
                        + "Regards,\n"
                        + "TaskFlow Team"
        );
        return message;
    }
}
