package org.team14.webty.security.oauth2

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.test.util.ReflectionTestUtils
import org.team14.webty.common.cookies.CookieManager
import org.team14.webty.common.enums.TokenType
import org.team14.webty.security.policy.ExpirationPolicy
import org.team14.webty.security.token.JwtManager
import org.team14.webty.user.enumerate.SocialProviderType
import org.team14.webty.user.service.UserService
import java.io.IOException
import java.util.*

@ExtendWith(MockitoExtension::class)
internal class LoginSuccessHandlerTest {

    @Mock
    private lateinit var cookieManager: CookieManager

    @Mock
    private lateinit var jwtManager: JwtManager

    @Mock
    private lateinit var userService: UserService

    @Mock
    private lateinit var request: HttpServletRequest

    @Mock
    private lateinit var response: HttpServletResponse

    @InjectMocks
    private lateinit var loginSuccessHandler: LoginSuccessHandler

    private lateinit var authenticationToken: OAuth2AuthenticationToken

    @BeforeEach
    fun setUp() {
        ReflectionTestUtils.setField(loginSuccessHandler, "REDIRECT_URI", "callbackURI")

        val attributes = mapOf("id" to "123456789")
        val oAuth2User: OAuth2User = DefaultOAuth2User(
            listOf(SimpleGrantedAuthority("ROLE_USER")), attributes, "id"
        )

        authenticationToken = OAuth2AuthenticationToken(
            oAuth2User, oAuth2User.authorities, "kakao"
        )
    }

    @Test
    @Throws(IOException::class)
    fun `OAuth2 로그인 - 기존 유저`() {
        val userId = 100L
        val accessToken = "mockAccessToken"
        val refreshToken = "mockRefreshToken"

        Mockito.`when`(userService.existSocialProvider("123456789")).thenReturn(Optional.of(userId))
        Mockito.`when`(jwtManager.createAccessToken(userId)).thenReturn(accessToken)
        Mockito.`when`(jwtManager.createRefreshToken(userId)).thenReturn(refreshToken)

        loginSuccessHandler.onAuthenticationSuccess(request, response, authenticationToken)

        Mockito.verify(userService).existSocialProvider("123456789")
        Mockito.verify(jwtManager).createAccessToken(userId)
        Mockito.verify(jwtManager).createRefreshToken(userId)
        Mockito.verify(cookieManager).setCookie(TokenType.ACCESS_TOKEN, accessToken, ExpirationPolicy.getAccessTokenExpirationTime())
        Mockito.verify(cookieManager).setCookie(TokenType.REFRESH_TOKEN, refreshToken, ExpirationPolicy.getRefreshTokenExpirationTime())
    }

    @Test
    @Throws(IOException::class)
    fun `OAuth2 로그인 - 신규 유저`() {
        val newUserId = 200L
        val accessToken = "mockAccessTokenNew"
        val refreshToken = "mockRefreshTokenNew"

        Mockito.`when`(userService.existSocialProvider("123456789")).thenReturn(Optional.empty())
        Mockito.`when`(userService.createUser(SocialProviderType.KAKAO, "123456789")).thenReturn(newUserId)
        Mockito.`when`(jwtManager.createAccessToken(newUserId)).thenReturn(accessToken)
        Mockito.`when`(jwtManager.createRefreshToken(newUserId)).thenReturn(refreshToken)

        loginSuccessHandler.onAuthenticationSuccess(request, response, authenticationToken)

        Mockito.verify(userService).existSocialProvider("123456789")
        Mockito.verify(userService).createUser(SocialProviderType.KAKAO, "123456789")
        Mockito.verify(jwtManager).createAccessToken(newUserId)
        Mockito.verify(jwtManager).createRefreshToken(newUserId)
        Mockito.verify(cookieManager).setCookie(TokenType.ACCESS_TOKEN, accessToken, ExpirationPolicy.getAccessTokenExpirationTime())
        Mockito.verify(cookieManager).setCookie(TokenType.REFRESH_TOKEN, refreshToken, ExpirationPolicy.getRefreshTokenExpirationTime())
    }
}
