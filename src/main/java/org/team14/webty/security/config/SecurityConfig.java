package org.team14.webty.security.config;

import java.util.List;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.team14.webty.security.authentication.CustomAuthenticationFilter;
import org.team14.webty.security.oauth2.LoginSuccessHandler;
import org.team14.webty.security.oauth2.LogoutSuccessHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final LoginSuccessHandler loginSuccessHandler;
	private final CustomAuthenticationFilter customAuthenticationFilter;
	private final LogoutSuccessHandler logoutSuccessHandler;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.authorizeHttpRequests(authorizeRequests ->
				authorizeRequests
					.anyRequest() // 나머지
					.authenticated())
			.sessionManagement(sessionManagement ->
				sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.headers(headers ->
				headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
			)
			.csrf(AbstractHttpConfigurer::disable)
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.oauth2Login(oauth2Login ->
				oauth2Login.successHandler(loginSuccessHandler))
			.logout(logout -> logout
				.addLogoutHandler(logoutSuccessHandler)
				.invalidateHttpSession(true)
				.logoutSuccessUrl("http://localhost:3000")
				.logoutSuccessHandler((request, response, authentication) -> {
					response.setStatus(HttpStatus.OK.value());
				})
			);

		return http.build();
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() { // 스프링 시큐리티를 무시할 페이지 목록 ( = 로그인이 필요없는 페이지 목록)
		return web -> web.ignoring().requestMatchers(
				"/v3/**", "/swagger-ui/**", "/api/logistics",
				"h2-console/**", "/error",
				"/webtoons/**", "/reviews/{id:\\d+}", "/reviews", "/reviews/view-count-desc",
				"/reviews/search", "/reviews/webtoon/{id:\\d+}",
				"/reviews/spoiler/{id:\\d+}"
			)
			.requestMatchers(HttpMethod.GET, "/similar")
			.requestMatchers(HttpMethod.GET, "/reviews/{reviewId}/comments")
			.requestMatchers(PathRequest.toH2Console());
	}

	@Bean
	public FilterRegistrationBean<CustomAuthenticationFilter> registration(CustomAuthenticationFilter filter) {
		FilterRegistrationBean<CustomAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of("http://localhost:3000")); // 허용할 Origin
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH")); // 허용할 HTTP 메소드
		configuration.setAllowedHeaders(List.of("*")); // 모든 헤더 허용
		configuration.setAllowCredentials(true); // 인증 정보 포함 허용

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration); // 모든 요청에 대해 적용

		return source;
	}
}
