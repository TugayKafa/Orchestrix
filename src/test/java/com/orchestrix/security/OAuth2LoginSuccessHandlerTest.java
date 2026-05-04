package com.orchestrix.security;

import com.orchestrix.entity.AuthProvider;
import com.orchestrix.entity.RefreshToken;
import com.orchestrix.entity.Role;
import com.orchestrix.entity.User;
import com.orchestrix.service.RefreshTokenService;
import com.orchestrix.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OAuth2LoginSuccessHandlerTest {

    @Mock
    UserService userService;

    @Mock
    JwtService jwtService;

    @Mock
    RefreshTokenService refreshTokenService;

    @InjectMocks
    OAuth2LoginSuccessHandler successHandler;

    @Test
    void testGoogleLoginCreatesNewUser() throws Exception {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("email")).thenReturn("ivan@gmail.com");
        when(oAuth2User.getAttribute("given_name")).thenReturn("Ivan");
        when(oAuth2User.getAttribute("family_name")).thenReturn("Ivanov");

        OAuth2AuthenticationToken authentication = buildAuthToken(oAuth2User, "google");

        User user = new User("ivan@gmail.com", null, "Ivan", "Ivanov",
                Role.USER, AuthProvider.GOOGLE);

        when(userService.findByEmail("ivan@gmail.com")).thenReturn(Optional.empty());
        when(userService.createOAuthUser("ivan@gmail.com", "Ivan", "Ivanov",
                AuthProvider.GOOGLE)).thenReturn(user);
        when(jwtService.generateToken("ivan@gmail.com", Role.USER)).thenReturn("access-token");
        when(refreshTokenService.generateRefreshToken(user))
                .thenReturn(new RefreshToken(user, "refresh-token", 1));

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(userService).createOAuthUser("ivan@gmail.com", "Ivan", "Ivanov",
                AuthProvider.GOOGLE);
        verify(response).sendRedirect("/index.html?accessToken=access-token&refreshToken=refresh-token");
    }

    @Test
    void testGoogleLoginUsesExistingUser() throws Exception {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("email")).thenReturn("ivan@gmail.com");
        when(oAuth2User.getAttribute("given_name")).thenReturn("Ivan");
        when(oAuth2User.getAttribute("family_name")).thenReturn("Ivanov");

        OAuth2AuthenticationToken authentication = buildAuthToken(oAuth2User, "google");

        User user = new User("ivan@gmail.com", null, "Ivan", "Ivanov",
                Role.USER, AuthProvider.GOOGLE);

        when(userService.findByEmail("ivan@gmail.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("ivan@gmail.com", Role.USER)).thenReturn("access-token");
        when(refreshTokenService.generateRefreshToken(user))
                .thenReturn(new RefreshToken(user, "refresh-token", 1));

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(userService, never()).createOAuthUser(any(), any(), any(), any());
        verify(response).sendRedirect("/index.html?accessToken=access-token&refreshToken=refresh-token");
    }

    @Test
    void testGithubLoginCreatesNewUserWithFullName() throws Exception {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("email")).thenReturn("ivan@github.com");
        when(oAuth2User.getAttribute("name")).thenReturn("Ivan Ivanov");

        OAuth2AuthenticationToken authentication = buildAuthToken(oAuth2User, "github");

        User user = new User("ivan@github.com", null, "Ivan", "Ivanov",
                Role.USER, AuthProvider.GITHUB);

        when(userService.findByEmail("ivan@github.com")).thenReturn(Optional.empty());
        when(userService.createOAuthUser("ivan@github.com", "Ivan", "Ivanov",
                AuthProvider.GITHUB)).thenReturn(user);
        when(jwtService.generateToken("ivan@github.com", Role.USER)).thenReturn("access-token");
        when(refreshTokenService.generateRefreshToken(user))
                .thenReturn(new RefreshToken(user, "refresh-token", 1));

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(userService).createOAuthUser("ivan@github.com", "Ivan", "Ivanov",
                AuthProvider.GITHUB);
        verify(response).sendRedirect("/index.html?accessToken=access-token&refreshToken=refresh-token");
    }

    @Test
    void testGithubLoginCreatesNewUserWithSingleName() throws Exception {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("email")).thenReturn("ivan@github.com");
        when(oAuth2User.getAttribute("name")).thenReturn("Ivan");

        OAuth2AuthenticationToken authentication = buildAuthToken(oAuth2User, "github");

        User user = new User("ivan@github.com", null, "Ivan", "",
                Role.USER, AuthProvider.GITHUB);

        when(userService.findByEmail("ivan@github.com")).thenReturn(Optional.empty());
        when(userService.createOAuthUser("ivan@github.com", "Ivan", "",
                AuthProvider.GITHUB)).thenReturn(user);
        when(jwtService.generateToken("ivan@github.com", Role.USER)).thenReturn("access-token");
        when(refreshTokenService.generateRefreshToken(user))
                .thenReturn(new RefreshToken(user, "refresh-token", 1));

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(userService).createOAuthUser("ivan@github.com", "Ivan", "",
                AuthProvider.GITHUB);
        verify(response).sendRedirect("/index.html?accessToken=access-token&refreshToken=refresh-token");
    }

    private OAuth2AuthenticationToken buildAuthToken(OAuth2User oAuth2User, String registrationId) {
        return new OAuth2AuthenticationToken(oAuth2User, oAuth2User.getAuthorities(), registrationId);
    }
}