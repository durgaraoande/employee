package com.abdr.employee.security.service;

import com.abdr.employee.security.dao.RoleRepo;
import com.abdr.employee.security.dao.UserRepo;
import com.abdr.employee.security.entities.ERole;
import com.abdr.employee.security.entities.Role;
import com.abdr.employee.security.entities.User;
import com.abdr.employee.security.utils.LoginRequest;
import com.abdr.employee.security.utils.RegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RoleRepo roleRepository;

    @Autowired
    private JwtService jwtService;

    public boolean register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            log.error("user not found {}", registerRequest.getUsername());
            return false; // Return false if user already exists
        }
        var user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .roles(getRoles())
                .build();
        userRepository.save(user);
        log.info("user registered {}", user.getUsername());
        return true;

    }

    private Set<Role> getRoles() {
        Set<Role> roles = new HashSet<>();
        // Use the roleRepository to get the role by its name
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Role not found!"));
        roles.add(userRole);
        return roles;
    }

    public String login(LoginRequest loginRequest) {
        try {
            log.debug("Attempting to find user: {}", loginRequest.getUsername());
            var user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> {
                        log.error("User not found: {}", loginRequest.getUsername());
                        return new UsernameNotFoundException("User not found: " + loginRequest.getUsername());
                    });

            log.debug("User found: {}", user.getUsername());
            log.info("Roles for user {}: {}", user.getUsername(), user.getRoles());


            // Create authentication token
            var authToken = new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            );

            log.debug("Attempting authentication with AuthenticationManager");
            Authentication authenticate = authenticationManager.authenticate(authToken);

            if (authenticate == null || !authenticate.isAuthenticated()) {
                log.error("Authentication failed for user: {}", loginRequest.getUsername());
                throw new BadCredentialsException("Authentication failed");
            }

            log.info("Authentication successful for user: {}", loginRequest.getUsername());
            String token = jwtService.generateToken(loginRequest.getUsername());
            log.debug("JWT token generated successfully");

            return token;

        } catch (UsernameNotFoundException | BadCredentialsException e) {
            log.error("Authentication error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during authentication", e);
            throw new RuntimeException("Authentication failed", e);
        }
    }

}
