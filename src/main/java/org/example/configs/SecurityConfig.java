package org.example.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
// In Spring Boot 2.7, we use EnableGlobalMethodSecurity instead of EnableMethodSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors().and() // Older syntax for enabling CORS
                // Disable CSRF for Stateless APIs
                .csrf().disable()
                .authorizeRequests(auth -> auth
                        // Changed requestMatchers to antMatchers
                        .antMatchers("/api/auth/**").permitAll() // Add this line!
                        .antMatchers("/public/**").permitAll()
                        .antMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                // Configure the app as an OAuth2 Resource Server
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt() // Default JWT configuration for Spring Security 5.x
                );

        return http.build();
    }
}