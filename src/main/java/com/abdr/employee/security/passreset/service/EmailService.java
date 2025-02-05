package com.abdr.employee.security.passreset.service;

import com.abdr.employee.utils.EmailSendException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

@Service
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.reset-password-template}")
    private String resetPasswordTemplate;

    @Value("${app.frontend.reset-password-url}")
    private String resetPasswordUrl;

    @Autowired
    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request");

            Context context = new Context();
            context.setVariable("resetLink", resetPasswordUrl + "?token=" + token);
            context.setVariable("expirationHours", 24); // Match with your token expiry setting

            String emailContent = templateEngine.process(resetPasswordTemplate, context);
            helper.setText(emailContent, true); // true indicates HTML content

            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", toEmail);

        } catch (MailException e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new EmailSendException("Failed to send password reset email", e);
        } catch (MessagingException e) {
            log.error("Error creating password reset email message for: {}", toEmail, e);
            throw new EmailSendException("Error creating password reset email", e);
        }
    }

    @Async
    public void sendPasswordChangeConfirmationEmail(String toEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Change Confirmation");

            Context context = new Context();
            context.setVariable("timestamp", LocalDateTime.now());

            String emailContent = templateEngine.process("password-change-confirmation", context);
            helper.setText(emailContent, true);

            mailSender.send(message);
            log.info("Password change confirmation email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send password change confirmation email to: {}", toEmail, e);
            // Don't throw exception for confirmation emails as it's not critical
        }
    }
}
