package com.campusconnect.campus.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableMethodSecurity
public class SecurityConfig {

 private final JwtRequestFilter jwtRequestFilter;


 public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
     this.jwtRequestFilter = jwtRequestFilter;
 }
@Bean
 public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
     http
             .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth->
                    auth.requestMatchers("/api/auth/**").permitAll()
                      .anyRequest().authenticated()
                  )
             .sessionManagement(session->
                             session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                     )
             .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
     // chaining of methods
     return http.build();
 }



    // isse spring smj jayega ki ye method ko khud call krke help of
    // SecurityConfig.passwordEncoder() call krke nya object banega and usse store kr dega apne paas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // It's a class that implements interface passwordEncoder
    }
}
