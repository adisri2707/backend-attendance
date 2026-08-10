package com.aditi.attendance.firebase.filter;

import com.aditi.attendance.firebase.dto.FirebaseUser;
import com.aditi.attendance.firebase.security.FirebaseUserDetails;
import com.aditi.attendance.firebase.service.FirebaseService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;

@Component
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private final FirebaseService firebaseService;
    private final boolean securityEnabled;

    public FirebaseAuthenticationFilter(
            FirebaseService firebaseService,
            @Value("${app.security.enabled:false}") boolean securityEnabled) {
        this.firebaseService = firebaseService;
        this.securityEnabled = securityEnabled;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !securityEnabled || request.getRequestURI().startsWith("/api/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String idToken = authorizationHeader.substring(7);

        try {

            FirebaseUser firebaseUser = firebaseService.verifyToken(idToken);

            FirebaseUserDetails userDetails =
                    new FirebaseUserDetails(firebaseUser);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch (Exception exception) {

            SecurityContextHolder.clearContext();

        }

        filterChain.doFilter(request, response);
    }

}
