package com.example.task.config;

import com.example.task.service.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = 7;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String JWT_EXPIRED_MESSAGE = "JWT expired";
    private static final String INVALID_SIGNATURE_MESSAGE = "Invalid signature";
    private static final String INVALID_TOKEN_MESSAGE = "Invalid token";
    private static final int SC_UNAUTHORIZED = HttpServletResponse.SC_UNAUTHORIZED;
    private static final int SC_FORBIDDEN = HttpServletResponse.SC_FORBIDDEN;
    private static final int SC_BAD_REQUEST = HttpServletResponse.SC_BAD_REQUEST;

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        String username = null;
        String jwt = null;

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            jwt = authHeader.substring(BEARER_PREFIX_LENGTH);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (ExpiredJwtException ex) {
                logger.warn("JWT token expired: " + ex.getMessage());
                response.sendError(SC_UNAUTHORIZED, JWT_EXPIRED_MESSAGE);
                return;
            } catch (SignatureException ex) {
                logger.warn("Invalid JWT signature: " + ex.getMessage());
                response.sendError(SC_FORBIDDEN, INVALID_SIGNATURE_MESSAGE);
                return;
            } catch (Exception ex) {
                logger.warn("Invalid JWT token: " + ex.getMessage());
                response.sendError(SC_BAD_REQUEST, INVALID_TOKEN_MESSAGE);
                return;
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        chain.doFilter(request, response);
    }
}