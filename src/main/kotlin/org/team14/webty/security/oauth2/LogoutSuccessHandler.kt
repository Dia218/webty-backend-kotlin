package org.team14.webty.security.oauth2

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.LogoutHandler
import org.springframework.stereotype.Component
import org.team14.webty.common.cookies.CookieManager
import org.team14.webty.common.enums.TokenType
import org.team14.webty.security.token.JwtManager
import java.util.concurrent.TimeUnit

@Component
class LogoutSuccessHandler(
    private val cookieManager: CookieManager,
    private val redisTemplate: RedisTemplate<String, String>,
    private val jwtManager: JwtManager
) : LogoutHandler {
    
    override fun logout(request: HttpServletRequest, response: HttpServletResponse, authentication: Authentication?) {
        // access-token 쿠키 삭제
        cookieManager.removeCookie(TokenType.ACCESS_TOKEN)
        
        // refresh-token 쿠키 삭제 및 Redis에 저장
        cookieManager.getCookieByTokenType(TokenType.REFRESH_TOKEN)?.let { refreshToken ->
            val expirationTime = jwtManager.getExpirationTime(refreshToken)
            redisTemplate.opsForValue().set(refreshToken, "logout", expirationTime, TimeUnit.MILLISECONDS)
            cookieManager.removeCookie(TokenType.REFRESH_TOKEN)
        }
        
        // JSESSIONID 쿠키 삭제
        cookieManager.removeCookie(TokenType.JSESSIONID)
    }
}
