package gr.hua.dit.project.core.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * JWT authentication filter.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;

    public JwtAuthenticationFilter(final JwtService jwtService) {
        if (jwtService == null) throw new NullPointerException();
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            final String token = authorizationHeader.substring(7);
            try {
                final Claims claims = this.jwtService.parse(token);
                final String subject = claims.getSubject();
                final Collection<String> roles = (Collection<String>) claims.get("roles");
                //Convert String to GrantedAuthority
                final var authorities =
                        roles == null
                        ? List.<GrantedAuthority>of() //empty list
                        : roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_"+role)).toList();
                //Create User
                final User principal = new User(subject,"",authorities);
                final UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } catch (Exception e) {
                // Invalid token or internal error.
                LOGGER.warn("JwtAuthenticationFilter failed", e);
                response.setStatus(401);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"invalid_token\"}");
                return; //stop here, i.e., next filters are ignored.
            }
        }
        filterChain.doFilter(request, response); //next filter.
    }

}
