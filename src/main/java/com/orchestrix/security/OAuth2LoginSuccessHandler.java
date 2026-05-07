package com.orchestrix.security;

import com.orchestrix.entity.auth.AuthProvider;
import com.orchestrix.entity.auth.OAuthUserInfo;
import com.orchestrix.entity.auth.User;
import com.orchestrix.service.auth.RefreshTokenServiceImpl;
import com.orchestrix.service.userService.UserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);
    private static final String ATTR_EMAIL = "email";
    private static final String ATTR_GIVEN_NAME = "given_name";
    private static final String ATTR_FAMILY_NAME = "family_name";
    private static final String ATTR_NAME = "name";
    private static final String GITHUB_NAME_SEPARATOR = " ";
    private static final String GITHUB_EMAILS_API = "https://api.github.com/user/emails";

    private final UserServiceImpl userService;
    private final JwtService jwtService;
    private final RefreshTokenServiceImpl refreshTokenService;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final RestTemplate restTemplate;

    public OAuth2LoginSuccessHandler(
            UserServiceImpl userService, JwtService jwtService, RefreshTokenServiceImpl refreshTokenService,
            OAuth2AuthorizedClientService authorizedClientService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.authorizedClientService = authorizedClientService;
        this.restTemplate = new RestTemplate();
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
            userInfo = extractDataFromGithubResponse(oAuth2User, (OAuth2AuthenticationToken) authentication);
        }

        User user = userService.findByEmail(userInfo.email())
                .orElseGet(() -> {
                    LOGGER.info("New OAuth2 user registered via {}: {}", provider, userInfo.email());
                    return userService.createOAuthUser(
                            userInfo.email(), userInfo.firstName(), userInfo.lastName(), provider);
                });

        LOGGER.info("OAuth2 login via {}: {}", provider, user.getEmail());

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

    private OAuthUserInfo extractDataFromGithubResponse(OAuth2User oAuth2User,
                                                          OAuth2AuthenticationToken authToken) {
        String email = oAuth2User.getAttribute(ATTR_EMAIL);

        if (email == null) {
            email = fetchGithubEmail(authToken);
        }

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

    private String fetchGithubEmail(OAuth2AuthenticationToken authToken) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authToken.getAuthorizedClientRegistrationId(), authToken.getName());

        String accessToken = client.getAccessToken().getTokenValue();

        RequestEntity<Void> request = RequestEntity.get(URI.create(GITHUB_EMAILS_API))
                .header(HttpHeaders.AUTHORIZATION, SecurityConstants.TOKEN_TYPE + accessToken)
                .build();

        List<Map<String, Object>> emails = restTemplate.exchange(
                request, new ParameterizedTypeReference<List<Map<String, Object>>>() { }).getBody();

        if (emails != null) {
            for (Map<String, Object> entry : emails) {
                if (Boolean.TRUE.equals(entry.get("primary"))) {
                    return (String) entry.get(ATTR_EMAIL);
                }
            }
        }

        LOGGER.warn("Could not fetch primary email from GitHub");
        return null;
    }
}
