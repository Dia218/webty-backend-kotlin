package org.team14.webty.common.cookies

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import org.team14.webty.common.enums.TokenType

@Component
@RequestScope
class CookieManager(
    private val response: HttpServletResponse,
    private val request: HttpServletRequest
) {
    fun getCookieByTokenType(tokenType: TokenType): String? {
        return request.cookies
            ?.takeIf { it.isNotEmpty() } // 조건 만족하면 그대로 반환 아니면 null
            ?.toList()
            ?.find { it.name == tokenType.type }
            ?.value
    }

    fun setCookie(tokenType: TokenType, token: String, expiration: Long) {
        val cookie = ResponseCookie.from(tokenType.type, token)
            .maxAge(expiration)
            .domain("localhost")
            .path("/")
            .secure(true)
            .httpOnly(true)
            .build()

        response.addHeader("Set-Cookie", cookie.toString())
    }

    fun removeCookie(tokenType: TokenType) {
        val cookie = ResponseCookie.from(tokenType.type, "")
            .maxAge(0)
            .domain("localhost")
            .path("/")
            .secure(true)
            .httpOnly(true)
            .build()

        response.addHeader("Set-Cookie", cookie.toString())
    }
}