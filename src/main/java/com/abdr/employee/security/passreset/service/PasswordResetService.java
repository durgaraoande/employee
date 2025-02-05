package com.abdr.employee.security.passreset.service;

import com.abdr.employee.security.dao.UserRepo;
import com.abdr.employee.security.entities.User;
import com.abdr.employee.security.passreset.dao.PasswordResetTokenRepository;
import com.abdr.employee.security.passreset.dto.PasswordResetConfirmRequest;
import com.abdr.employee.security.passreset.entity.PasswordResetToken;
import com.abdr.employee.security.passreset.utils.PasswordResetCompletedEvent;
import com.abdr.employee.security.passreset.utils.PasswordResetInitiatedEvent;
import com.abdr.employee.utils.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class PasswordResetService {
    private final UserRepo userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.password-reset.token-expiry-hours:24}")
    private int tokenExpiryHours;

    @Autowired
    public PasswordResetService(UserRepo userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                EmailService emailService,
                                PasswordEncoder passwordEncoder,
                                ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    public void initiatePasswordReset(String email) {
        log.info("Initiating password reset for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Password reset attempted for non-existent email: {}", email);
                    return new ResourceNotFoundException("User not found with email: " + email);
                });

        // Delete any existing token for the user before creating a new one
        tokenRepository.deleteByUser(user);
        log.debug("Deleted existing password reset token for user: {}", user.getEmail());

    try {
        String token = generateUniqueToken();
        log.debug("Token used {}",token);
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        log.debug("Attempting to save token");
        tokenRepository.save(resetToken);
        log.debug("Token saved..");


            emailService.sendPasswordResetEmail(user.getEmail(), token);
            log.info("Password reset email sent successfully to: {}", email);
            eventPublisher.publishEvent(new PasswordResetInitiatedEvent(user));
        }
    catch (DataIntegrityViolationException e) {
        log.warn("Concurrent password reset attempt for email: {}", email);
        throw new ConcurrentResetException("A password reset is already in progress. Please wait a moment and try again.");
    }
    catch (Exception e) {
            log.error("Failed to send password reset email to: {}", email, e);
            throw new EmailSendException("Failed to send password reset email", e);
        }
    }

    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        log.info("Processing password reset confirmation");

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            log.warn("Password reset failed: Passwords don't match");
            throw new InvalidPasswordException("New password and confirm password don't match");
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> {
                    log.warn("Password reset attempted with invalid token");
                    return new InvalidTokenException("Invalid or expired password reset token");
                });

        if (resetToken.isExpired() || resetToken.isUsed()) {
            log.warn("Password reset attempted with expired or used token");
            tokenRepository.delete(resetToken);
            throw new InvalidTokenException("Invalid or expired password reset token");
        }

        User user = resetToken.getUser();
        String newEncodedPassword = passwordEncoder.encode(request.getNewPassword());

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            log.warn("Password reset attempted with same password for user: {}", user.getEmail());
            tokenRepository.delete(resetToken);
            throw new InvalidPasswordException("New password must be different from the current password");
        }

        user.setPassword(newEncodedPassword);
//        resetToken.setUsed(true);

        userRepository.save(user);
//        tokenRepository.save(resetToken);

        tokenRepository.delete(resetToken);

        log.info("Password reset successful for user: {}", user.getEmail());
        eventPublisher.publishEvent(new PasswordResetCompletedEvent(user));
    }

    @Scheduled(cron = "0 0 */1 * * *") // Run every hour
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired password reset tokens");
        List<PasswordResetToken> expiredTokens =
                tokenRepository.findAllByExpiryDateBeforeAndUsedFalse(LocalDateTime.now());

        if (!expiredTokens.isEmpty()) {
            tokenRepository.deleteAll(expiredTokens);
            log.info("Cleaned up {} expired password reset tokens", expiredTokens.size());
        }
    }

//    private String generateUniqueToken() {
//        return UUID.randomUUID().toString();
//    }

    // Use more secure token generation
    private String generateUniqueToken() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public void validateToken(String token) {
        // Retrieve the token from the repository
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Password reset attempted with invalid token");
                    return new InvalidTokenException("Invalid or expired password reset token");
                });

        // Check if the token is expired or already used
        if (resetToken.isExpired()) {
            log.warn("Password reset token is expired");
            throw new InvalidTokenException("The password reset token has expired.");
        }

        if (resetToken.isUsed()) {
            log.warn("Password reset token has already been used");
            throw new InvalidTokenException("The password reset token has already been used.");
        }

        log.info("Password reset token is valid");
    }

}