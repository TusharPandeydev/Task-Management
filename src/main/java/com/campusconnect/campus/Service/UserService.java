package com.campusconnect.campus.Service;




import com.campusconnect.campus.Dto.Request.LoginRequestDTO;
import com.campusconnect.campus.Dto.Request.RegisterRequestDTO;
import com.campusconnect.campus.Dto.Response.AuthTokenDto;
import com.campusconnect.campus.Dto.Response.RegisterResponseDTO;
import com.campusconnect.campus.Dto.Response.ValidateRefreshTokenDto;
import com.campusconnect.campus.Enum.Role;
import com.campusconnect.campus.Exception.EmailAlreadyExist;
import com.campusconnect.campus.Exception.InvalidCredentialsException;
import com.campusconnect.campus.Model.RefreshToken;
import com.campusconnect.campus.Model.User;
import com.campusconnect.campus.Repositories.RefreshTokenRepository;
import com.campusconnect.campus.Repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    // we need UserRepository class dependency which can be  from Spring itself only
    private final JWTService jwtService;
    private final EmailVerificationService emailVerificationService;
    private final UserRepository userRepository ;
    private final PasswordEncoder passwordEncoder ;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenHashService refreshTokenHashService;
    private final LoginNotificationService loginNotificationService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       EmailVerificationService emailVerificationService,
                       JWTService jwtService,
                       RefreshTokenRepository refreshTokenRepository,
                       RefreshTokenHashService refreshTokenHashService,
                       LoginNotificationService loginNotificationService) {
        // Parametrized Constructor and using dependency injection here
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationService=emailVerificationService;
        this.jwtService = jwtService;
        this.refreshTokenRepository=refreshTokenRepository;
        this.refreshTokenHashService=refreshTokenHashService;
        this.loginNotificationService=loginNotificationService;
    }

    public RegisterResponseDTO registerUser(RegisterRequestDTO registerRequest){
        // ismein email hamesha unique honi chahiye
        Optional<User> checkEmailExist = userRepository.findByEmail(registerRequest.getEmail());
        if(checkEmailExist.isPresent()){
            throw new EmailAlreadyExist("Email already exists");
        }

           User user =new User();
           user.setEmail(registerRequest.getEmail());
           user.setFullName(registerRequest.getFullName());
           user.setPhoneNumber(registerRequest.getPhoneNumber());
           user.setRole(Role.ROLE_EMPLOYEE);
         // WE CANNOT SAVE PASSWORD DIRECTLY IN DATABASE SECURITY PURPOSE IT IS CONCERNING TO DO
            String hashedPassword = passwordEncoder.encode(registerRequest.getPassword());
            user.setPassword(hashedPassword);
            user.setEmailVerified(false); // IMPORTANT FOR EMAIL VERIFICATION PROCESS !!
        User savedUser=userRepository.save(user);

            // remember save() method hamesha entity return krta hain jisse save kr rhe the h
           // NOW , email given by user we will see kya ye valid hain ya nahi random email toh nahi de dii
           // by using concept of verification OTP AND STMP ka use krke

          emailVerificationService.sendForVerification(savedUser);

         RegisterResponseDTO registerResponse = new RegisterResponseDTO();
         String message="User registered successfully ! Please check your mail for confirmation";
         registerResponse.setMessage(message);
          registerResponse.setFullName(savedUser.getFullName());
         registerResponse.setEmail(savedUser.getEmail());
         registerResponse.setPhoneNumber(savedUser.getPhoneNumber());
     return  registerResponse;

    }

    // IF USER IS NOT NEW THEN USE THIS
    public AuthTokenDto loginUser(LoginRequestDTO loginRequest){
        // HttpServletResponse -->ek interface hain
        Optional<User> checkEmailExist = userRepository.findByEmail(loginRequest.getEmail());
      // by this we get optional class object address
        if(checkEmailExist.isEmpty()){
            throw new InvalidCredentialsException("Invalid Email or Password ");
        }
        User user=checkEmailExist.get(); // by this we will get entity object of the USER

        if(!user.getEmailVerified()){ // verified user hii login karega
            throw new InvalidCredentialsException("Verify your account");
        }

        boolean checkPassword=passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
        if(!checkPassword){
            throw new InvalidCredentialsException("Invalid Email or Password");
        }

        AuthTokenDto authTokenDto=new AuthTokenDto();
        authTokenDto.setUserName(user.getFullName());
        authTokenDto.setUserRole(user.getRole().name());
        authTokenDto.setEmailID(loginRequest.getEmail());

        // GENERATING ACCESS TOKEN IN THIS -->ye isliye kiya kyuki yaha toh refresh token expired OR
        //  user is first time logging in
        String accessToken=jwtService.generateAccessToken(user);
        authTokenDto.setAccessToken(accessToken);

        // refresh token is given in HttpOnly direct to browser so it get stored there
       // agr multi device hua toh user ek hii account se multiple device logged in kr skta hain jisse multiple
        // refresh token honge -->ensure kii hamesha nya hii banao token

        String rawToken= UUID.randomUUID().toString();
        String tokenHash=refreshTokenHashService.hashToken(rawToken);
        LocalDateTime createdAt=LocalDateTime.now();
        LocalDateTime expiresAt= createdAt.plusDays(7);
        String familyId=UUID.randomUUID().toString();

        RefreshToken refreshToken =new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(expiresAt);
        refreshToken.setCreatedAt(createdAt);
        refreshToken.setUsed(false);
        refreshToken.setRevoked(false);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setFamilyId(familyId);
        refreshTokenRepository.save(refreshToken);
        authTokenDto.setRefreshToken(rawToken);// real token is this one
        LocalDateTime loginTime=LocalDateTime.now();
        loginNotificationService.sendLoginNotification(user,loginTime);

        return authTokenDto;

    }


    // below function will works perfectly fine if user is currently having refresh token that :
    // 1. exist in database
    // 2. not expired yet
    // 3. not used yet
    // 4. not revoked till now
    // if above all are successfully handled then we can then only genrate refresh token new one
    @Transactional
    public ValidateRefreshTokenDto validateRefreshToken(String refreshToken){

        // first====> find that refresh token is present in database ?if it is then get the object of that token
        String generateTokenHash=refreshTokenHashService.hashToken(refreshToken);
    RefreshToken refreshTokenObj = refreshTokenRepository.findByTokenHash(generateTokenHash)
            .orElseThrow(() -> new InvalidCredentialsException("Session Expired ! LOGIN AGAIN"));
   // first check is token expired or not
     if(refreshTokenObj.getExpiresAt().isBefore(LocalDateTime.now())){
         throw new InvalidCredentialsException("Invalid Refresh Token");
     }
    // second --> is token revoked ??
     if(refreshTokenObj.isRevoked()){
         throw new InvalidCredentialsException("Invalid Refresh Token");
     }
     // third --> is this token is used one ??
     if(refreshTokenObj.isUsed()){
         String familyId=refreshTokenObj.getFamilyId();
          refreshTokenRepository.revokeRefreshToken(familyId);
          throw new InvalidCredentialsException("Invalid Refresh Token");
     }
     User user=refreshTokenObj.getUser();



        // NOW IMPORTANT THING COMES AND THAT IS --->REFRESH TOKEN RORATION LOGIC!!
        // Again make refresh token
        ValidateRefreshTokenDto validateDto=new ValidateRefreshTokenDto();
        String rawToken=UUID.randomUUID().toString();
        String tokenHash=refreshTokenHashService.hashToken(rawToken);
        //generate new refresh token by generating new refresh token object

        refreshTokenObj.setUsed(true);// old refresh toke ko true declare

        RefreshToken refreshToken1=new RefreshToken();
        refreshToken1.setUser(user);
        LocalDateTime createdAt=LocalDateTime.now();
        LocalDateTime expiresAt= createdAt.plusDays(7);
        refreshToken1.setCreatedAt(createdAt);
        refreshToken1.setExpiresAt(expiresAt);
        refreshToken1.setUsed(false);
        refreshToken1.setTokenHash(tokenHash);
        refreshToken1.setRevoked(false);
        refreshToken1.setFamilyId(refreshTokenObj.getFamilyId());
        refreshTokenRepository.save(refreshToken1);

        validateDto.setRefreshToken(rawToken);// real token will get saved in browser

        String accessToken=jwtService.generateAccessToken(user);
        validateDto.setAccessToken(accessToken);


        return validateDto;
    }
}
