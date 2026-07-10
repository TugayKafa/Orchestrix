package com.orchestrix.security;

import com.orchestrix.entity.auth.Role;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtRequestFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtRequestFilter.class);

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtRequestFilter(JwtService jwtService, TokenBlacklistService tokenBlacklistService) {
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);
        if (authHeader == null || !authHeader.startsWith(SecurityConstants.TOKEN_TYPE)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(SecurityConstants.TOKEN_TYPE.length());
            String email = jwtService.extractEmail(token);
            Role role = jwtService.extractRole(token);

            if (tokenBlacklistService.isBlacklisted(token)) {
                LOGGER.warn("Blacklisted token used by: {}", email);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                email, null,
                                AuthorityUtils.createAuthorityList(SecurityConstants.ROLE_PREFIX + role.name()));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (JwtException exc) {
            LOGGER.warn("Invalid JWT token: {}", exc.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
