package org.team14.webty.security.oauth2

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.security.core.Authentication
import org.team14.webty.common.cookies.CookieManager
import org.team14.webty.common.enums.TokenType
import org.team14.webty.security.token.JwtManager
import java.util.concurrent.TimeUnit

@ExtendWith(MockitoExtension::class)
class LogoutSuccessHandlerTest {

    @Mock
    private lateinit var cookieManager: CookieManager

    @Mock
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @Mock
    private lateinit var jwtManager: JwtManager

    @Mock
    private lateinit var valueOperations: ValueOperations<String, String>

    @InjectMocks
    private lateinit var logoutSuccessHandler: LogoutSuccessHandler

    @Mock
    private lateinit var request: HttpServletRequest

    @Mock
    private lateinit var response: HttpServletResponse

    @Mock
    private lateinit var authentication: Authentication

    @BeforeEach
    fun setUp() {
        whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)
    }

    @Test
    fun `should handle logout successfully`() {
        /*
         * 로그아웃을 호출했을 때 정상적으로 작동하는 지 테스트
         * 1. 쿠키 삭제 확인
         * 2. `redis`에 로그아웃 정보를 저장했는지 확인
         */

        // Given
        val refreshToken = "mockRefreshToken"
        val expirationTime = 1000L

        whenever(cookieManager.getCookieByTokenType(TokenType.REFRESH_TOKEN)).thenReturn(refreshToken)
        whenever(jwtManager.getExpirationTime(refreshToken)).thenReturn(expirationTime)

        // When
        logoutSuccessHandler.logout(request, response, authentication)

        // Then
        verify(cookieManager).removeCookie(TokenType.ACCESS_TOKEN)
        verify(cookieManager).removeCookie(TokenType.REFRESH_TOKEN)
        verify(cookieManager).removeCookie(TokenType.JSESSIONID)
        verify(valueOperations).set(refreshToken, "logout", expirationTime, TimeUnit.MILLISECONDS)
    }
}
