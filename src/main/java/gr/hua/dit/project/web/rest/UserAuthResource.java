package gr.hua.dit.project.web.rest;

import gr.hua.dit.project.core.security.JwtService;
import gr.hua.dit.project.web.rest.model.ClientTokenRequest;
import gr.hua.dit.project.web.rest.model.ClientTokenResponse;
import gr.hua.dit.project.web.rest.model.UserLoginRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserAuthResource {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserAuthResource(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<ClientTokenResponse> login(@RequestBody @Valid UserLoginRequest request){

        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );

            UserDetails user = (UserDetails) authentication.getPrincipal();

            String roles = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));

            String token = jwtService.issue(user.getUsername(), Collections.singleton(roles));

            return ResponseEntity.ok(new ClientTokenResponse(token, "Bearer", 3600));

        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid username or password");
        }

    }

}
