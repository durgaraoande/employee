package com.abdr.employee.security.passreset.controller;

import com.abdr.employee.security.captcha.CaptchaService;
import com.abdr.employee.security.passreset.dto.PasswordResetConfirmRequest;
import com.abdr.employee.security.passreset.dto.PasswordResetRequest;
import com.abdr.employee.security.passreset.service.PasswordResetService;
import com.abdr.employee.security.ratelimit.RateLimiterService;
import com.abdr.employee.utils.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/auth/password")
@Slf4j
public class PasswordResetController {

    private final PasswordResetService passwordResetService;
    private final CaptchaService captchaService;
    private final RateLimiterService rateLimiterService;

    @Autowired
    public PasswordResetController(PasswordResetService passwordResetService, CaptchaService captchaService, RateLimiterService rateLimiterService) {
        this.passwordResetService = passwordResetService;
        this.captchaService = captchaService;
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/reset/request")
    public ModelAndView requestPasswordReset(
            @Valid @ModelAttribute("passwordResetRequest") PasswordResetRequest request,
            @RequestParam("g-recaptcha-response") String captchaResponse,
            HttpServletRequest httpRequest,
            Model model) {
        log.debug("Received password reset request for email: {}", request.getEmail());

        String clientIp = getClientIp(httpRequest);

        try {
            // Verify CAPTCHA
            if (!captchaService.validateCaptcha(captchaResponse)) {
                model.addAttribute("error", "Please verify that you are not a robot");
                return new ModelAndView("error", model.asMap());
            }

            // Check rate limit
            rateLimiterService.checkRateLimit(clientIp, "FORGOT_PASSWORD");

            passwordResetService.initiatePasswordReset(request.getEmail());
            model.addAttribute("message", "Password reset instructions have been sent to your email.");
            return new ModelAndView("password-reset-request-success", model.asMap());
        } catch (ResourceNotFoundException e) {
            // Return same message even if email doesn't exist (security best practice)
            log.warn("Password reset requested for non-existent email: {}", request.getEmail());
            model.addAttribute("message", "Password reset instructions have been sent to your email.");
            return new ModelAndView("password-reset-request-success", model.asMap());
        } catch (RateLimitExceededException | ConcurrentResetException e) {
            model.addAttribute("error", e.getMessage());
            return new ModelAndView("error", model.asMap());
        }
        catch (Exception e) {
            log.error("Error processing password reset request", e);
            model.addAttribute("error", "Error processing password reset request. Please try again.");
            return new ModelAndView("error", model.asMap());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }


    @GetMapping("/reset/request")
    public String showPasswordResetRequestForm(Model model) {
        model.addAttribute("passwordResetRequest", new PasswordResetRequest());
        return "password-reset-request-form";
    }

    @GetMapping("/reset/confirm")
    public String showPasswordResetConfirmForm(@RequestParam String token, Model model) {
        // Validate token before showing form
        try {
            passwordResetService.validateToken(token);
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
            request.setToken(token);
            model.addAttribute("passwordResetConfirmRequest", request);
            model.addAttribute("token", token);
            return "password-reset-confirm-form";
        } catch (InvalidTokenException e) {
            model.addAttribute("error", "Invalid or expired password reset token.");
            return "password-reset-confirm-failure";
        }
    }

    @PostMapping("/reset/confirm")
    public ModelAndView confirmPasswordReset(
            @Valid @ModelAttribute("passwordResetConfirmRequest") PasswordResetConfirmRequest request,
            @RequestParam("g-recaptcha-response") String captchaResponse,
            HttpServletRequest httpServletRequest,
            Model model) {
        log.debug("Processing password reset confirmation");

        try {

            // Verify CAPTCHA
            if (!captchaService.validateCaptcha(captchaResponse)) {
                model.addAttribute("error", "Please verify that you are not a robot");
                return new ModelAndView("error", model.asMap());
            }

            passwordResetService.confirmPasswordReset(request);
            model.addAttribute("message", "Password has been reset successfully.");
            return new ModelAndView("password-reset-confirm-success", model.asMap());
        } catch (InvalidTokenException e) {
            log.warn("Invalid token used for password reset");
            model.addAttribute("error", "Invalid or expired password reset token.");
            return new ModelAndView("password-reset-confirm-failure", model.asMap());
        } catch (InvalidPasswordException e) {
            log.warn("Invalid password provided for reset");
            model.addAttribute("error", e.getMessage());
            return new ModelAndView("password-reset-confirm-failure", model.asMap());
        } catch (Exception e) {
            log.error("Error confirming password reset", e);
            model.addAttribute("error", "Error processing password reset confirmation. Please try again.");
            return new ModelAndView("error", model.asMap());
        }
    }
}
