package br.com.segsat.restwhitspringbootandjava.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import br.com.segsat.restwhitspringbootandjava.security.jwt.JwtConfigurer;
import br.com.segsat.restwhitspringbootandjava.security.jwt.JwtTokenProvider;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        Map<String, PasswordEncoder> encoders = new HashMap<>();
		encoders.put("pbkdf2", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8());
		DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder("pbkdf2", encoders);
		passwordEncoder.setDefaultPasswordEncoderForMatches(Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8());
        return passwordEncoder;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeHttpRequests(authorize -> {
                    try {
                        authorize
                            .requestMatchers(
                                "/auth/signin",
                                 "/auth/refresh/**", 
                                 "/v3/api-docs/**",
                                 "/swagger-ui**",
                                 "/swagger-ui/**").permitAll()
                            .requestMatchers("/api/**").authenticated()
                            .requestMatchers("/users").denyAll()
                            .and()
                                .cors()
                            .and()
                                .apply(new JwtConfigurer(tokenProvider));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });                      
        // .requestMatchers("/db/**").access(new
        // WebExpressionAuthorizationManager("hasRole('ADMIN') and hasRole('DBA')"))
        // .requestMatchers("/db/**").access(AuthorizationManagers.allOf(AuthorityAuthorizationManager.hasRole("ADMIN"),
        // AuthorityAuthorizationManager.hasRole("DBA")))
        // .anyRequest().denyAll()
        // .authorizeHttpRequests((authz) -> authz
        // .anyRequest().authenticated()
        // );
        return http.build();
    }

}