
package com.nivocode.auth;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        return http
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        "/api/auth/register",
                        "/api/auth/login",
                        "/api/products",
                        "/api/products/**",
                        "/api/customers",
                        "/api/customers/**"
                ))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        ))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(
                                DispatcherType.ERROR
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/auth/me",
                                "/api/products",
                                "/api/products/{id}",
                                "/api/customers",
                                "/api/customers/{id}"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/products",
                                "/api/customers"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/products/{id}",
                                "/api/customers/{id}"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/products/{id}",
                                "/api/customers/{id}"
                        ).authenticated()

                        .anyRequest().denyAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .build();
    }
}
