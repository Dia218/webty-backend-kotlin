package org.team14.webty.security.config

import org.springframework.boot.autoconfigure.security.servlet.PathRequest
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
    private val logoutSuccessHandler: LogoutSuccessHandler
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
        return WebSecurityCustomizer {
            it.ignoring().requestMatchers(
                "/v3/**", "/swagger-ui/**", "/api/logistics",
                "h2-console/**", "/error",
                "/webtoons/**", "/reviews/{id:\\d+}", "/reviews", "/reviews/view-count-desc",
                "/reviews/search", "/reviews/webtoon/{id:\\d+}",
                "/reviews/spoiler/{id:\\d+}"
            )
                .requestMatchers(HttpMethod.GET, "/similar")
                .requestMatchers(HttpMethod.GET, "/reviews/{reviewId}/comments")
                .requestMatchers(PathRequest.toH2Console())
        }
    }

    @Bean
    fun registration(filter: CustomAuthenticationFilter): FilterRegistrationBean<CustomAuthenticationFilter> =
        FilterRegistrationBean(filter).apply { isEnabled = false }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource = UrlBasedCorsConfigurationSource().apply {
        registerCorsConfiguration("/**", CorsConfiguration().apply {
            allowedOrigins = listOf("http://localhost:3000")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH")
            allowedHeaders = listOf("*")
            allowCredentials = true
        })
    }
}
