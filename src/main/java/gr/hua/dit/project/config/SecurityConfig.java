package gr.hua.dit.project.config;

import gr.hua.dit.project.core.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * API chain {@code "/api/**"} (stateless, JWT).
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiChain(final HttpSecurity http,
                                        final JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .securityMatcher("/api/v1/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/token").permitAll()
                        .requestMatchers("/api/v1/**").authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);
        return http.build();
    }

    /**
     * UI chain {@code "/**"} (stateful, cookie based).
     */
    @Bean
    @Order(2)
    public SecurityFilterChain uiChain(final HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/",
                                "/login",
                                "/register",
                                "/restaurants/*/menu",
                                "/restaurants/*/cart/**",
                                "/css/**",
                                "/js/**"
                        ).permitAll()
                        .requestMatchers("/owner/**").hasRole("OWNER")
                        .requestMatchers("/customer/**").hasRole("CUSTOMER")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler((request, response, authentication) -> {
                            boolean isOwner = authentication.getAuthorities()
                                    .stream()
                                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_OWNER"));

                            boolean isCustomer = authentication.getAuthorities()
                                    .stream()
                                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_CUSTOMER"));

                            if (isOwner) {
                                response.sendRedirect("/owner/dashboard");
                            } else if (isCustomer) {
                                response.sendRedirect("/");
                            } else {
                                response.sendRedirect("/login");
                            }
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}