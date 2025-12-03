package gr.hua.dit.project.core.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/register",
                                "/login",
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
                                response.sendRedirect("/owner/profile");
                            } else if (isCustomer) {
                                response.sendRedirect("/customer/profile");
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
