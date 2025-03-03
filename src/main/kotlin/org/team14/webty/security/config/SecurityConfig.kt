package org.team14.webty.security.config

import org.springframework.boot.autoconfigure.security.servlet.PathRequest
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.team14.webty.security.authentication.CustomAuthenticationFilter
import org.team14.webty.security.oauth2.LoginSuccessHandler
import org.team14.webty.security.oauth2.LogoutSuccessHandler

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val loginSuccessHandler: LoginSuccessHandler,
    private val customAuthenticationFilter: CustomAuthenticationFilter,
    private val logoutSuccessHandler: LogoutSuccessHandler,
    private val environment: Environment
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain = http.run {
        addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        authorizeHttpRequests { it.anyRequest().authenticated() }
        sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        headers { it.frameOptions { frameOptions -> frameOptions.sameOrigin() } }
        csrf(AbstractHttpConfigurer<*, *>::disable)
        cors { it.configurationSource(corsConfigurationSource()) }
        oauth2Login { it.successHandler(loginSuccessHandler) }
        logout {
            it.addLogoutHandler(logoutSuccessHandler)
                .invalidateHttpSession(true)
                .logoutSuccessUrl("http://localhost:3000")
                .logoutSuccessHandler { _, response, _ -> response.status = HttpStatus.OK.value() }
        }
        build()
    }

    @Bean
    fun webSecurityCustomizer(): WebSecurityCustomizer {
        return WebSecurityCustomizer { web ->
            val ignoringConfigurer = web.ignoring()
            // 공통으로 무시할 URL 패턴들
            ignoringConfigurer.requestMatchers(
                "/v3/**", "/swagger-ui/**", "/api/logistics",
                "/error",
                "/webtoons/**", "/reviews/{id:\\d+}", "/reviews", "/reviews/view-count-desc",
                "/reviews/search", "/reviews/webtoon/{id:\\d+}",
                "/reviews/spoiler/{id:\\d+}", "/search/**"
            )
            // 'application-test' 일 때, H2 콘솔 관련 매처 추가
            if (environment.activeProfiles.contains("test")) {
                ignoringConfigurer.requestMatchers("h2-console/**")
                ignoringConfigurer.requestMatchers(PathRequest.toH2Console())
            }
            ignoringConfigurer.requestMatchers(HttpMethod.GET, "/similar")
            ignoringConfigurer.requestMatchers(HttpMethod.GET, "/reviews/{reviewId}/comments")
        }
    }

    @Bean
    fun registration(filter: CustomAuthenticationFilter): FilterRegistrationBean<CustomAuthenticationFilter> =
        FilterRegistrationBean(filter).apply { isEnabled = false }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource = UrlBasedCorsConfigurationSource().apply {
        registerCorsConfiguration("/**", CorsConfiguration().apply {
            allowedOrigins = listOf("http://localhost:3000", "http://host.docker.internal:8080")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH")
            allowedHeaders = listOf("*")
            allowCredentials = true
        })
    }
}
