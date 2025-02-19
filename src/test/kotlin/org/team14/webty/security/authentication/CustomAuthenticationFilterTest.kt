package org.team14.webty.security.authentication

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.team14.webty.common.cookies.CookieManager
import org.team14.webty.common.enums.TokenType
import org.team14.webty.security.policy.ExpirationPolicy
import org.team14.webty.security.token.JwtManager
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class CustomAuthenticationFilterTest {

    @InjectMocks
    private lateinit var customAuthenticationFilter: CustomAuthenticationFilter

    @Mock
    private lateinit var jwtManager: JwtManager

    @Mock
    private lateinit var cookieManager: CookieManager

    @Mock
    private lateinit var request: HttpServletRequest

    @Mock
    private lateinit var response: HttpServletResponse

    @Mock
    private lateinit var filterChain: FilterChain

    @Mock
    private lateinit var authentication: Authentication

    private val accessToken = "validAccessToken"
    private val refreshToken = "validRefreshToken"
    private val newAccessToken = "newAccessToken"
    private val newRefreshToken = "newRefreshToken"

    @BeforeEach
    fun setUp() {
        SecurityContextHolder.clearContext() // SecurityContext 초기화
    }

    @Test
    fun `엑세스 토큰으로 인증`() {
        // Given
        whenever(request.getHeader("Authorization")).thenReturn("Bearer $accessToken")
        whenever(jwtManager.validate(accessToken)).thenReturn(true)
        whenever(jwtManager.getAuthentication(accessToken)).thenReturn(authentication)

        // When
        customAuthenticationFilter.doFilterInternal(request, response, filterChain)

        // Then
        assertEquals(authentication, SecurityContextHolder.getContext().authentication)
        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun `엑세스 토큰 만료 리프레시토큰으로 인증 및 엑세스 토큰 재발급`() {
        // Given
        whenever(request.getHeader("Authorization")).thenReturn(null)
        whenever(cookieManager.getCookieByTokenType(TokenType.ACCESS_TOKEN)).thenReturn(null)
        whenever(cookieManager.getCookieByTokenType(TokenType.REFRESH_TOKEN)).thenReturn(refreshToken)
        whenever(jwtManager.validate(refreshToken)).thenReturn(true)

        whenever(jwtManager.recreateTokens(refreshToken)).thenReturn(arrayOf(newAccessToken, newRefreshToken))
        whenever(jwtManager.validate(newAccessToken)).thenReturn(true)
        whenever(jwtManager.getAuthentication(newAccessToken)).thenReturn(authentication)

        // When
        customAuthenticationFilter.doFilterInternal(request, response, filterChain)

        // Then
        assertEquals(authentication, SecurityContextHolder.getContext().authentication)
        verify(cookieManager).setCookie(TokenType.ACCESS_TOKEN, newAccessToken, ExpirationPolicy.accessTokenExpirationTime)
        verify(cookieManager).setCookie(TokenType.REFRESH_TOKEN, newRefreshToken, ExpirationPolicy.refreshTokenExpirationTime)
        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun `위조된 엑세스토큰 올바른 리프레시 토큰`() {
        // Given: Access Token이 존재하지만 변조된 값
        whenever(request.getHeader("Authorization")).thenReturn("Bearer invalidAccessToken")
        whenever(jwtManager.validate("invalidAccessToken")).thenReturn(false)

        whenever(cookieManager.getCookieByTokenType(TokenType.REFRESH_TOKEN)).thenReturn(refreshToken)
        whenever(jwtManager.validate(refreshToken)).thenReturn(true)
        whenever(jwtManager.recreateTokens(refreshToken)).thenReturn(arrayOf(newAccessToken, newRefreshToken))
        whenever(jwtManager.validate(newAccessToken)).thenReturn(true)
        whenever(jwtManager.getAuthentication(newAccessToken)).thenReturn(authentication)

        // When
        customAuthenticationFilter.doFilterInternal(request, response, filterChain)

        // Then
        assertEquals(authentication, SecurityContextHolder.getContext().authentication)
        verify(cookieManager).setCookie(TokenType.ACCESS_TOKEN, newAccessToken, ExpirationPolicy.accessTokenExpirationTime)
        verify(cookieManager).setCookie(TokenType.REFRESH_TOKEN, newRefreshToken, ExpirationPolicy.refreshTokenExpirationTime)
        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun `만료 혹은 변조된 액세스 토큰 및 리프레시 토큰`() {
        // Given
        whenever(request.getHeader("Authorization")).thenReturn(null)
        whenever(cookieManager.getCookieByTokenType(TokenType.ACCESS_TOKEN)).thenReturn(null)
        whenever(cookieManager.getCookieByTokenType(TokenType.REFRESH_TOKEN)).thenReturn(null)

        // When
        customAuthenticationFilter.doFilterInternal(request, response, filterChain)

        // Then
        verify(response).status = HttpServletResponse.SC_UNAUTHORIZED
        verify(filterChain, never()).doFilter(request, response)
    }
}
