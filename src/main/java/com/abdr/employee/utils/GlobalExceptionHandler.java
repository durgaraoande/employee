package com.abdr.employee.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handle general exceptions
    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model) {
        logger.error("An error occurred: {}", ex.getMessage(), ex);
        // Add a general error message to the model for the view layer
        model.addAttribute("error", "An unexpected error occurred. Please try again later.");
        // Returning a user-friendly error page
        return "error";
    }

    // Handle constraint validation exceptions (e.g., form validation errors)
    @ExceptionHandler(ConstraintViolationException.class)
    public String handleConstraintException(ConstraintViolationException ex, Model model) {
        logger.warn("Validation failed: {}", ex.getMessage());

        List<String> validationErrors = new ArrayList<>();
        ex.getConstraintViolations().forEach(violation -> validationErrors.add(violation.getMessage()));

        // Pass validation errors to the model
        model.addAttribute("error", "Validation failed: " + String.join(", ", validationErrors));

        // Returning a user-friendly error page
        return "error";
    }

    // Handle custom exceptions (e.g., EmployeeNotFoundException)
    @ExceptionHandler(EmployeeNotFoundException.class)
    public String handleEmployeeNotFound(EmployeeNotFoundException ex, Model model) {
        logger.error("Employee not found: {}", ex.getMessage(), ex);
        model.addAttribute("error", ex.getMessage());
        return "error";
    }

    // Handle Access Denied (Authorization Denied) exceptions
//    @ExceptionHandler(AccessDeniedException.class)
//    public String handleAuthorizationDeniedException(AccessDeniedException ex, Model model) {
//        logger.warn("Access Denied: {}", ex.getMessage());
//
//        // Adding error message to the model
//        model.addAttribute("error", "You do not have permission to access this resource.");
//
//        // Returning a user-friendly error page (you can customize this as needed)
//        return "error";
//    }

//    @ExceptionHandler(AccessDeniedException.class)
//    public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException ex) {
//        logger.warn("Access Denied: {}", ex.getMessage());
//        Map<String, String> response = new HashMap<>();
//        response.put("status", "error");
//        response.put("message", "Access Denied: You do not have permission to perform this action");
//        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
//    }

    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request, Model model) {
        logger.warn("Access Denied: {}", ex.getMessage());

        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains("application/json")) {
            // Return JSON for API clients
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Access Denied: You do not have permission to perform this action");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } else {
            // Return view for browser clients
            model.addAttribute("error", "You do not have permission to access this resource.");
            return "error"; // Return the error view
        }
    }


    // Handle SecretKeyGeneratingException (for SecretKey generation failures)
    @ExceptionHandler(SecretKeyGeneratingException.class)
    public String secretKeyGeneratingExceptionHandler(SecretKeyGeneratingException e, Model model) {
        logger.error("Secret key generation failed: {}", e.getMessage(), e);

        // Set the error message and add it to the model
        model.addAttribute("error", "Secret key generation failed: " + e.getMessage());

        // Returning the error page view to display the error message
        return "error";
    }

    // Handle JwtAndSignatureHandler exceptions (for JWT and signature validation errors)
    @ExceptionHandler(JwtAndSignatureHandler.class)
    public String handleJwtAndSignatureException(JwtAndSignatureHandler ex, Model model) {
        logger.error("JWT or signature error: {}", ex.getMessage(), ex);

        // Set the error message and add it to the model
        model.addAttribute("error", "JWT or signature error: " + ex.getMessage());

        // Returning the error page view to display the error message
        return "error";
    }

//    @ExceptionHandler(UnauthorizedAccessException.class)
//    public String handleUnauthorizedAccess(UnauthorizedAccessException ex, Model model) {
//        model.addAttribute("error","Unauthorized "+ ex.getMessage());
//        return "error";
//    }
}
