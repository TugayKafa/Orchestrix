package com.orchestrix.security;

import com.orchestrix.entity.AuthProvider;
import com.orchestrix.entity.User;
import com.orchestrix.service.RefreshTokenService;
import com.orchestrix.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public OAuth2LoginSuccessHandler(
            UserService userService, JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");

        User user = userService.findByEmail(email)
                .orElseGet(() -> userService.createOAuthUser(email, firstName, lastName, AuthProvider.GOOGLE));

        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole());
        String refreshToken = refreshTokenService.generateRefreshToken(user).getToken();

        response.sendRedirect("/index.html?accessToken=" + accessToken + "&refreshToken=" + refreshToken);
    }
}
