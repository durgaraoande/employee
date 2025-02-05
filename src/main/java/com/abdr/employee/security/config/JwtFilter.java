package com.abdr.employee.security.config;

import com.abdr.employee.controllers.EmpController;
import com.abdr.employee.security.service.JwtService;
import com.abdr.employee.security.service.MyUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
public class JwtFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ApplicationContext context;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = null;
        String userName = null;

        try {
            // First check Authorization header
            String authHeader = request.getHeader("Authorization");
            log.debug("Authorization Header: {}", authHeader);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                log.debug("Extracted Token: {}", token);
            }

            // If no token in header, check session
            if (token == null) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    token = (String) session.getAttribute("jwt_token");
                    log.debug("Token from Session: {}", token);
                }
            }

            // If we have a token, try to validate it
            if (token != null) {
                userName = jwtService.extractUserName(token);
                log.debug("Username from token: {}", userName);

                // Validate token and set authentication if valid
                if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = context.getBean(MyUserDetailsService.class)
                            .loadUserByUsername(userName);

                    log.debug("User details loaded. Authorities: {}", userDetails.getAuthorities());

                    if (jwtService.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } else {
                log.warn("No JWT token found in request");
            }

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            log.error("JWT Filter error", ex);
            handleJwtException(response, "Authentication failed: " + ex.getMessage());
        }
    }

    private void handleJwtException(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{ \"error\": \"" + message + "\" }");
        response.getWriter().flush();
    }
}

