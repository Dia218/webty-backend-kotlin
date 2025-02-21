package org.team14.webty.security.token

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import org.team14.webty.common.exception.BusinessException
import org.team14.webty.common.exception.ErrorCode
import org.team14.webty.security.authentication.WebtyUserDetails
import org.team14.webty.security.authentication.WebtyUserDetailsService
import org.team14.webty.security.policy.ExpirationPolicy
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

@Component
class JwtManager(
    private val webtyUserDetailsService: WebtyUserDetailsService,
    private val redisTemplate: RedisTemplate<String, String>
) {
    @Value("\${jwt.secret}")
    private lateinit var secret: String
    private lateinit var secretKey: javax.crypto.SecretKey

    @PostConstruct
    fun init() {
        secretKey = Keys.hmacShaKeyFor(secret.toByteArray())
        println("JWT 보안 키가 성공적으로 생성되었습니다.")
    }

    fun createAccessToken(userId: Long): String {
        return Jwts.builder().apply {
            claim("userId", userId)
            issuedAt(Date.from(Instant.now()))
            expiration(Date.from(Instant.now().plusMillis(ExpirationPolicy.accessTokenExpirationTime)))
            signWith(secretKey)
        }.compact()
    }

    fun createRefreshToken(userId: Long): String {
        val expirationTime = ExpirationPolicy.refreshTokenExpirationTime
        val refreshToken = Jwts.builder().apply {
            claim("userId", userId)
            issuedAt(Date(System.currentTimeMillis()))
            expiration(Date(System.currentTimeMillis() + expirationTime))
            signWith(secretKey)
        }.compact()

        redisTemplate.opsForValue().set(refreshToken, userId.toString(), expirationTime, TimeUnit.MILLISECONDS)
        return refreshToken
    }


    fun getExpirationTime(token: String): Long {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
                .expiration.time
        } catch (e: JwtException) {
            println("인증 토큰이 유효하지 않거나 만료되었습니다: ${e.message}")
            throw BusinessException(ErrorCode.TOKEN_NOT_VALID)
        }
    }

    fun validate(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
            !isExpired(token)
        } catch (e: JwtException) {
            println("인증 토큰 검증에 실패했습니다: ${e.message}")
            false
        }
    }

    private fun isExpired(token: String): Boolean {
        val expiration = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
            .expiration
        return expiration.before(Date())
    }

    fun recreateTokens(refreshToken: String): Array<String> {
        val userId = getUserIdByToken(refreshToken)
        val newAccessToken = createAccessToken(userId)
        val newRefreshToken = createRefreshToken(userId)

        return arrayOf(newAccessToken, newRefreshToken)
    }

    fun getUserIdByToken(token: String): Long {
        return try {
            val claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload

            (claims["userId"] as Number).toLong()  // 안전한 변환
        } catch (e: JwtException) {
            println("인증 토큰에서 사용자 정보를 가져오는데 실패했습니다: ${e.message}")
            throw BusinessException(ErrorCode.TOKEN_NOT_VALID)
        }
    }

    fun getAuthentication(accessToken: String): Authentication {
        val webtyUserDetails: WebtyUserDetails = webtyUserDetailsService.loadUserByUserId(
            getUserIdByToken(accessToken)
        )
        return UsernamePasswordAuthenticationToken(
            webtyUserDetails, "",
            webtyUserDetails.authorities
        )
    }
}
