package com.abdr.employee.security.controller;

import com.abdr.employee.security.dao.UserRepo;
import com.abdr.employee.security.passreset.service.PasswordResetService;
import com.abdr.employee.security.service.AuthService;
import com.abdr.employee.security.utils.LoginRequest;
import com.abdr.employee.security.utils.RegisterRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private AuthService authService;

    @GetMapping("/login")
    public String showLoginForm(
            @RequestParam(value = "error", required = false) String error,
            Model model) {
        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute("loginRequest", new LoginRequest());
        }

        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }

        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(@Valid @ModelAttribute("loginRequest") LoginRequest loginRequest,
                              BindingResult result,
                              Model model,
                              HttpSession session,
                              HttpServletResponse httpResponse,
                              RedirectAttributes redirectAttributes) {
        log.debug("Custom handleLogin method invoked for user: {}", loginRequest.getUsername());


        if (result.hasErrors()) {
            return "login";
        }

        try {
            String token = authService.login(loginRequest);

            if (token == null || token.isEmpty()) {
                model.addAttribute("error", "Invalid credentials!");
                return "login";
            }

            // Store token in session
            session.setAttribute("jwt_token", token);

            // Optional: Set token in an HTTP-only cookie for added security
            Cookie jwtCookie = new Cookie("jwt_token", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(2 * 60 * 60); // 2 hours
            httpResponse.addCookie(jwtCookie);

            return "redirect:/home";

        } catch (UsernameNotFoundException | BadCredentialsException e) {
            model.addAttribute("loginRequest", loginRequest);
            model.addAttribute("error", "Invalid username or password");
            return "login";
        } catch (Exception e) {
            model.addAttribute("loginRequest", loginRequest);
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            return "login";
        }
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(@Valid @ModelAttribute RegisterRequest registerRequest, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "register"; // Return to the registration form if validation fails
        }

        boolean registered = authService.register(registerRequest);
        if (!registered) {
            model.addAttribute("error", "Username or email already exists!");
            return "register"; // Show error message if registration fails
        }

        return "redirect:/auth/login"; // Redirect to login page after successful registration
    }


}
