package com.orchestrix.security;

import com.orchestrix.entity.AuthProvider;
import com.orchestrix.entity.OAuthUserInfo;
import com.orchestrix.entity.User;
import com.orchestrix.service.RefreshTokenService;
import com.orchestrix.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);
    private static final String ATTR_EMAIL = "email";
    private static final String ATTR_GIVEN_NAME = "given_name";
    private static final String ATTR_FAMILY_NAME = "family_name";
    private static final String ATTR_NAME = "name";
    private static final String GITHUB_NAME_SEPARATOR = " ";

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

        OAuthUserInfo userInfo;
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        AuthProvider provider = extractProvider(authentication);

        if (provider == AuthProvider.GOOGLE) {
            userInfo = extractDataFromGoogleResponse(oAuth2User);
        } else {
            userInfo = extractDataFromGithubResponse(oAuth2User);
        }

        User user = userService.findByEmail(userInfo.email())
                .orElseGet(() -> {
                    logger.info("New OAuth2 user registered via {}: {}", provider, userInfo.email());
                    return userService.createOAuthUser(
                            userInfo.email(), userInfo.firstName(), userInfo.lastName(), provider);
                });

        logger.info("OAuth2 login via {}: {}", provider, user.getEmail());

        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole());
        String refreshToken = refreshTokenService.generateRefreshToken(user).getToken();

        response.sendRedirect("/index.html?accessToken=" + accessToken + "&refreshToken=" + refreshToken);
    }

    private AuthProvider extractProvider(Authentication authentication) {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        return  AuthProvider.valueOf(registrationId.toUpperCase());
    }

    private OAuthUserInfo extractDataFromGoogleResponse(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute(ATTR_EMAIL);
        String firstName = oAuth2User.getAttribute(ATTR_GIVEN_NAME);
        String lastName = oAuth2User.getAttribute(ATTR_FAMILY_NAME);
        return new OAuthUserInfo(email, firstName, lastName);
    }

    private OAuthUserInfo extractDataFromGithubResponse(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute(ATTR_EMAIL);
        String fullName = oAuth2User.getAttribute(ATTR_NAME);
        String firstName = "";
        String lastName = "";

        if (fullName != null && fullName.contains(GITHUB_NAME_SEPARATOR)) {
            firstName = fullName.substring(0, fullName.indexOf(GITHUB_NAME_SEPARATOR));
            lastName = fullName.substring(fullName.indexOf(GITHUB_NAME_SEPARATOR) + 1);
        } else if (fullName != null) {
            firstName = fullName;
        }

        return new OAuthUserInfo(email, firstName, lastName);
    }
}
