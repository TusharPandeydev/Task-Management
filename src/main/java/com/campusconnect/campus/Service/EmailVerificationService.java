package com.campusconnect.campus.Service;

import com.campusconnect.campus.Dto.Request.EmailVerifyRequestDto;
import com.campusconnect.campus.Dto.Request.ResendOtpDto;
import com.campusconnect.campus.Exception.AlreadyVerifiedUserException;
import com.campusconnect.campus.Exception.InvalidCredentialsException;
import com.campusconnect.campus.Exception.TimeOutException;
import com.campusconnect.campus.Exception.UserNotFoundException;
import com.campusconnect.campus.Model.EmailVerification;
import com.campusconnect.campus.Model.User;
import com.campusconnect.campus.Repositories.EmailVerificationRepository;
import com.campusconnect.campus.Repositories.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
public class EmailVerificationService {
    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final JavaMailSender javaMailSender; // VERY IMPORTANT

     @Value("${spring.mail.username}")
     private  String senderMail;

    // Spring Boot Mail autoconfiguration provides a configured
    // JavaMailSender bean, which Spring injects through constructor

    private final SecureRandom secureRandom = new SecureRandom();
    // CONSTRUCTOR ::
    public EmailVerificationService(EmailVerificationRepository emailVerificationRepository,
                                    JavaMailSender javaMailSender,
                                    UserRepository userRepository) {
        this.emailVerificationRepository = emailVerificationRepository;
        this.javaMailSender = javaMailSender;
        this.userRepository = userRepository;
    }
    public  void sendForVerification(User user){


        int getOtp=100000+secureRandom.nextInt(900000); // 6 DIGIT CODE
        String otp=String.valueOf(getOtp);
        LocalDateTime endTime= LocalDateTime.now().plusMinutes(10);

        EmailVerification emailVerification=new EmailVerification(); //entity
        emailVerification.setUser(user);
        emailVerification.setOtp(otp);
        emailVerification.setExpireAt(endTime);
        emailVerificationRepository.save(emailVerification);

        try {
            SimpleMailMessage message = new SimpleMailMessage();


            message.setFrom(senderMail);
            message.setTo(user.getEmail());
            message.setSubject("Email Verification OTP");
            message.setText(
                    "Your Verification OTP is: " + otp +
                            "\nThis OTP will expire in 10 minutes."
            );

            javaMailSender.send(message);
            System.out.println(">>> SUCCESS: Email sent to " + user.getEmail());

        } catch (Exception e) {
            System.err.println(">>> ERROR sending email: " + e.getMessage());
            e.printStackTrace(); // Console par full error tracing dikhegi
        }

    }


    public void checkOtp(EmailVerifyRequestDto emailVerifyRequestDto)  {
        LocalDateTime now=LocalDateTime.now();
        Optional<User> getUser=userRepository.findByEmail(emailVerifyRequestDto.getEmail());
        if(getUser.isEmpty()){
            throw new UserNotFoundException("User not found");
        }
        User currentUser=getUser.get(); // USER EXIST THIS MEANS IN DATABASE !!
        // Hibernate uses the User entity's @Id to query
        // the EmailVerification relationship through the user_id foreign key

        Optional<EmailVerification> getEmailVerification=emailVerificationRepository.findByUserId(currentUser.getId());
        if(getEmailVerification.isEmpty()){

            throw new InvalidCredentialsException("PLEASE ENTER VALID EMAIL");
        }
        EmailVerification currentEmail=getEmailVerification.get();
        if(currentEmail.getExpireAt().isBefore(now)) {
            emailVerificationRepository.delete(currentEmail);// this means pehle expire time finish
            throw new TimeOutException("Email verification expired");
        }
        if(!Objects.equals(currentEmail.getOtp(), emailVerifyRequestDto.getOtp())){
            throw  new InvalidCredentialsException("Please Enter Correct OTP");
        }
        currentUser.setEmailVerified(true);
        userRepository.save(currentUser); // existing code mein update hoon jayega thats all
        // MEANS YAHA TK SB SAHI SO OTP VERIFICATION IS SUCCESSFUL
        // verification gets successful then : -->SO NOW NO USE OF THAT OTP TO GETS STORED
        emailVerificationRepository.delete(currentEmail);
    }

    public void resendOtpVerification(ResendOtpDto resendOtpDto){
        // sbse first check kro user valid hain ya nahi
        Optional<User> getUserObject=userRepository.findByEmail(resendOtpDto.getEmail());
        if(getUserObject.isEmpty()){
            throw new UserNotFoundException("User not found");
        }
        User currentUser=getUserObject.get();
        if(currentUser.getEmailVerified()){ // MEANS PEHLE SE VERIFIED USER HUA TOH
            throw new AlreadyVerifiedUserException(currentUser.getFullName()+" is already verified");
        }

        // best alternative of optional class ==>in this you don't need to explicit mention of Optional class and get()
        // method
        EmailVerification currentEmail=emailVerificationRepository.findByUser(currentUser)
                .orElseThrow(()->new UserNotFoundException("User not found"));

        // Now in sbke baad toh hm pkka hain ki --->user ko otp wapis bhejni hogi
        int getNewOtp=100000+secureRandom.nextInt(900000);
        String otp=String.valueOf(getNewOtp);
        LocalDateTime endTime= LocalDateTime.now().plusMinutes(10);
        currentEmail.setOtp(otp);
        currentEmail.setExpireAt(endTime);
        emailVerificationRepository.save(currentEmail);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderMail);
        message.setTo(resendOtpDto.getEmail());
        message.setSubject("Email Verification");
        message.setText(
                "Your Verification OTP is: " + otp +
                        "\nThis OTP will expire in 10 minutes."
        );
        javaMailSender.send(message); //configured mail sender ko email send karne ko bolta hai
    }

}
