package com.abdr.employee.security.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        String errorMessage = "Invalid credentials";
        if (exception instanceof LockedException) {
            errorMessage = "Account is locked";
        } else if (exception instanceof DisabledException) {
            errorMessage = "Account is disabled";
//        } else if (exception instanceof BadCredentialsException) {
//            errorMessage = "Invalid username or password";
        }

        response.getWriter().write(String.format("{\"error\": \"Authentication failed\", \"message\": \"%s\"}", errorMessage));
    }
}
