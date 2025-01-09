package com.abdr.employee.utils;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

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
}
