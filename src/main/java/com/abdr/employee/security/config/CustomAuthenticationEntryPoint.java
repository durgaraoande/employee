package com.abdr.employee.security.config;

import com.abdr.employee.utils.UnauthorizedAccessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

//public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
//    @Override
//    public void commence(HttpServletRequest request, HttpServletResponse response,
//                         AuthenticationException authException) throws IOException {
////        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
////        response.setContentType("application/json;charset=UTF-8");
////        response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Authentication is required to access this resource\"}");
//        throw new UnauthorizedAccessException("Authentication is required to access this resource.");
//    }
//}

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // Set the HTTP response status as 401 Unauthorized
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Redirect the user to the custom error page with the error message as a query parameter
        response.sendRedirect(request.getContextPath() + "/error/unauthorized?message=" + authException.getMessage());
    }
}