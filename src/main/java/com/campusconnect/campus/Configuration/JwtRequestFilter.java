package com.campusconnect.campus.Configuration;



import com.campusconnect.campus.Repositories.UserRepository;
import com.campusconnect.campus.Service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    private final JWTService jwtService;
    private final UserRepository userRepository;
    public JwtRequestFilter(JWTService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
         String email=null;
         String token = null;
    if(authHeader != null && authHeader.startsWith("Bearer ")) {
        token = authHeader.substring(7);
        try{
            email=jwtService.extractEmails(token);
          // email extract kro username ki identity hai woh joh payload mein milegi


        }
        catch(Exception e){

            System.err.println("JWT Filter error: " + e.getMessage());
        }
    }
    if(email!=null && SecurityContextHolder.getContext().getAuthentication()==null){
        // means email mil chuki hain but ye user abhi tk authenticated nhi hain -->ismein check hoga
            // Ismein sbse pehle user object nikalo help of user email
        var userOptional=userRepository.findByEmail(email);
        if(userOptional.isPresent()){
            var user=userOptional.get();
            if(jwtService.tokenIsValid(token,user)){
                // iss service mein --->1 =>token kahi expire toh nahi hoon gya ?
                // 2=> token se email id extract krke and user ki email id dono match krenge and check ?
                // 3=> sbse important signature verify hota hain joh signature client bhejta hain and joh hm
                // calculate krte hain on th spot

                // if upr ke all three points shi toh user valid hain
                UsernamePasswordAuthenticationToken authentication=new UsernamePasswordAuthenticationToken(
                        user,null,user.getAuthorities());

                // authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));--->CODE!!

                // above line is showing error -->but its purpose is clear in which it --->fetch user IP ADDRESS
                // And from which browser request is coming --->help in security tracking
                SecurityContextHolder.getContext().setAuthentication(authentication); // user ko logged-IN mark kr diya
            }
        }
    }

        filterChain.doFilter(request,response);

    }
}
