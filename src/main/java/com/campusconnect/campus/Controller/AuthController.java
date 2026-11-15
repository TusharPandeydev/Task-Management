package com.campusconnect.campus.Controller;

import com.campusconnect.campus.Dto.Request.EmailVerifyRequestDto;
import com.campusconnect.campus.Dto.Request.LoginRequestDTO;
import com.campusconnect.campus.Dto.Request.RegisterRequestDTO;
import com.campusconnect.campus.Dto.Request.ResendOtpDto;
import com.campusconnect.campus.Dto.Response.AuthTokenDto;
import com.campusconnect.campus.Dto.Response.LoginResponseDTO;
import com.campusconnect.campus.Dto.Response.RegisterResponseDTO;
import com.campusconnect.campus.Dto.Response.ValidateRefreshTokenDto;
import com.campusconnect.campus.Service.EmailVerificationService;
import com.campusconnect.campus.Service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
   private final UserService userService;
    private final EmailVerificationService emailVerificationService;

    public AuthController(UserService userService,
      EmailVerificationService emailVerificationService) {
        this.userService=userService;
        this.emailVerificationService=emailVerificationService;
    }
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> registerUser(@Valid @RequestBody RegisterRequestDTO registerRequestDTO){
        RegisterResponseDTO registerResponseDTO=userService.registerUser(registerRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(registerResponseDTO);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyUser(@Valid @RequestBody EmailVerifyRequestDto emailVerifyRequestDto){
          emailVerificationService.checkOtp(emailVerifyRequestDto);
          String message="Successfully verified otp ! Please Login ";
          return ResponseEntity.status(HttpStatus.OK).body(message);
    }
    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtpToUser(@Valid @RequestBody ResendOtpDto resendOtpDto){
        emailVerificationService.resendOtpVerification(resendOtpDto);
        return  ResponseEntity.status(HttpStatus.OK).body("Successfully resent otp");
    }

    private static void generateRefreshToken(String refreshToken,int expirationTime,
                                             HttpServletResponse httpServletResponse){

        Cookie refreshTokenCookie = new Cookie("refreshToken",refreshToken);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(expirationTime);// agr hm aisa nahi krte toh session cookie bn jaata hain

        refreshTokenCookie.setHttpOnly(true);// javascript read hii na kr ske
        refreshTokenCookie.setSecure(false );// true rakhna jb HTTPS USE KRE hoon (production mein )
        // session cookie means browser close cookie delete apne aap
        // but jb hm age likh dete hain toh cookie tb tk rahegi and jb time khtm automatic vanish
        //refreshTokenCookie.setDomain("localhost");
        httpServletResponse.addCookie(refreshTokenCookie);
    }

    // NOW LOGIN
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> loginUser(@Valid @RequestBody LoginRequestDTO loginRequestDTO,
                                                      HttpServletResponse response){

        AuthTokenDto authTokenDto=userService.loginUser(loginRequestDTO);
        int time=7*24*60*60;

        String refreshToken=authTokenDto.getRefreshToken();
        generateRefreshToken(refreshToken,time,response);

        LoginResponseDTO loginResponseDTO=new LoginResponseDTO();
        loginResponseDTO.setRole(authTokenDto.getUserRole());
        loginResponseDTO.setEmail(authTokenDto.getEmailID());
        loginResponseDTO.setFullName(authTokenDto.getUserName());
        loginResponseDTO.setAccessToken(authTokenDto.getAccessToken());

            return ResponseEntity.status(HttpStatus.OK).body(loginResponseDTO);
    }

    // Acess token expired and refresh token rotaion
    private ResponseEntity<Map<String,Object>> buildResponse(HttpStatus status, String message){
        Map<String,Object> map=new HashMap<>();
        map.put("timestamp",System.currentTimeMillis());
        map.put("status",status.value()); // 401 etc WHY NOR JUST STATUS
        map.put("error",status.getReasonPhrase()); // reason in string format "Not found"
        map.put("message",message);
        return ResponseEntity.status(status).body(map);
    }
  @PostMapping("/refresh-token")
 public ResponseEntity<?> refreshToken(@CookieValue(name="refreshToken",required=false)String refreshToken,
                                       HttpServletResponse response){
        // agr cookie nahi bhi hogi toh spring boot error ko deal nahi krega hm manually deal krenge
      if(refreshToken==null || refreshToken.isBlank()){
          String message="Refresh Token is missing ! Please login again";
          return buildResponse(HttpStatus.UNAUTHORIZED,message);
      }
      ValidateRefreshTokenDto bothTokens= userService.validateRefreshToken(refreshToken);
      int time=7*24*60*60;
      generateRefreshToken(bothTokens.getRefreshToken(),time,response);
      // RULE--->json mein spaces nahi hone chahiye
      return ResponseEntity.ok(Map.of("accessToken",bothTokens.getAccessToken()));
    }

}
