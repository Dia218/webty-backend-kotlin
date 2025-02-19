package org.team14.webty.security.oauth2

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.team14.webty.common.cookies.CookieManager
import org.team14.webty.common.enums.TokenType
import org.team14.webty.security.policy.ExpirationPolicy
import org.team14.webty.security.token.JwtManager
import org.team14.webty.user.enumerate.SocialProviderType
import org.team14.webty.user.service.UserService

@Component
class LoginSuccessHandler(
    private val cookieManager: CookieManager,
    private val jwtManager: JwtManager,
    private val userService: UserService
) : SimpleUrlAuthenticationSuccessHandler() {
    @Value("\${jwt.redirect}")
    lateinit var redirectUri: String

    override fun onAuthenticationSuccess(request: HttpServletRequest, response: HttpServletResponse, authentication: Authentication) {
        val token = authentication as OAuth2AuthenticationToken
        val provider = token.authorizedClientRegistrationId

        registerTokens(request, response, getLoginUserId(token, provider))
    }

    private fun registerTokens(request: HttpServletRequest, response: HttpServletResponse, userId: Long) {
        val refreshToken = jwtManager.createRefreshToken(userId)
        val accessToken = jwtManager.createAccessToken(userId)

        cookieManager.setCookie(TokenType.ACCESS_TOKEN, accessToken, ExpirationPolicy.accessTokenExpirationTime)
        cookieManager.setCookie(TokenType.REFRESH_TOKEN, refreshToken, ExpirationPolicy.refreshTokenExpirationTime)

        redirectStrategy.sendRedirect(request, response, String.format(redirectUri))
    }

    private fun getLoginUserId(token: OAuth2AuthenticationToken, provider: String): Long {
        val providerId = ProviderUserInfo(token.principal.attributes).providerId

        val existUserId = userService.existSocialProvider(providerId)

        println("PROVIDER: $provider")
        println("PROVIDER_ID: $providerId")

        return existUserId.orElseGet {
            println("신규 유저입니다. 등록을 진행합니다.")
            userService.createUser(SocialProviderType.fromProviderName(provider), providerId)
        }
    }
}
