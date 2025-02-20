package org.team14.webty.security.authentication

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.team14.webty.common.cookies.CookieManager
import org.team14.webty.common.enums.TokenType
import org.team14.webty.security.policy.ExpirationPolicy
import org.team14.webty.security.token.JwtManager
import java.io.IOException

@Component
class CustomAuthenticationFilter(
    private val cookieManager: CookieManager,
    private val jwtManager: JwtManager
) : OncePerRequestFilter() {
    
    private val log = LoggerFactory.getLogger(CustomAuthenticationFilter::class.java)
    private val authorizationHeader = "Authorization"
    private val bearerPrefix = "Bearer "
    
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val excludePrefixes = listOf("/login") // 추가적인 제외 경로가 있으면 여기에 추가
        return excludePrefixes.any { request.requestURI.startsWith(it) }
    }
    
    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain
    ) {
        val accessToken = getAccessToken(request)
        var authentication = requestGetAuthentication(accessToken)
        
        // 리프레쉬 토큰을 통한 인증
        if (authentication == null) {
            val refreshToken = cookieManager.getCookieByTokenType(TokenType.REFRESH_TOKEN)
            val newAccessToken = getAccessTokenByRefreshToken(refreshToken)
            authentication = requestGetAuthentication(newAccessToken)
        }
        
        // 모든 토큰이 유효하지 않을 경우
        if (authentication == null) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return
        }
        
        // SecurityContext 설정 후 필터 진행
        SecurityContextHolder.getContext().apply {
            setAuthentication(authentication)
        }
        filterChain.doFilter(request, response)
    }
    
    private fun getAccessToken(request: HttpServletRequest): String? {
        return getAccessTokenFromHeader(request) ?: cookieManager.getCookieByTokenType(TokenType.ACCESS_TOKEN)
    }
    
    private fun getAccessTokenFromHeader(request: HttpServletRequest): String? {
        val token = request.getHeader(authorizationHeader)
        return if (!token.isNullOrBlank() && token.startsWith(bearerPrefix)) {
            token.removePrefix(bearerPrefix)
        } else {
            null
        }
    }
    
    private fun requestGetAuthentication(accessToken: String?): Authentication? {
        return if (!accessToken.isNullOrBlank() && jwtManager.validate(accessToken)) {
            log.debug("엑세스 토큰이 유효함")
            jwtManager.getAuthentication(accessToken)
        } else {
            log.debug("엑세스 토큰이 없거나 만료됨")
            null
        }
    }
    
    private fun getAccessTokenByRefreshToken(refreshToken: String?): String? {
        return if (!refreshToken.isNullOrBlank() && jwtManager.validate(refreshToken)) {
            log.debug("리프레쉬 토큰이 유효함")
            updateNewAccessToken(refreshToken)
        } else {
            log.debug("리프레쉬 토큰이 없거나 만료됨")
            null
        }
    }
    
    private fun updateNewAccessToken(refreshToken: String): String {
        val (newAccessToken, newRefreshToken) = jwtManager.recreateTokens(refreshToken)
        
        cookieManager.setCookie(TokenType.ACCESS_TOKEN, newAccessToken, ExpirationPolicy.accessTokenExpirationTime)
        log.debug("새로운 엑세스 토큰 발급 완료")
        
        cookieManager.setCookie(TokenType.REFRESH_TOKEN, newRefreshToken, ExpirationPolicy.refreshTokenExpirationTime)
        log.debug("새로운 리프레쉬 토큰 발급 완료")
        
        return newAccessToken
    }
}
