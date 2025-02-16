package org.team14.webty.security.authentication;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.team14.webty.common.cookies.CookieManager;
import org.team14.webty.common.enums.TokenType;
import org.team14.webty.security.policy.ExpirationPolicy;
import org.team14.webty.security.token.JwtManager;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

	private final CookieManager cookieManager;
	private final JwtManager jwtManager;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String requestURI = request.getRequestURI();

		List<String> excludePrefixes = List.of( // 여기에 다른 페이지는 추가하지 않습니다
			"/login"
		);

		return excludePrefixes.stream()
			.anyMatch(requestURI::startsWith);
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain) throws ServletException, IOException {

		String accessToken;
		Authentication authentication;

		// 엑세스 토큰으로 인증하기
		accessToken = getAccessToken(request);
		authentication = requestGetAuthentication(accessToken);

		// 리프레쉬 토큰으로 인증하기
		if (authentication == null) {
			String refreshToken = cookieManager.getCookieByTokenType(TokenType.REFRESH_TOKEN);
			accessToken = getAccessTokenByRefreshToken(refreshToken);
			authentication = requestGetAuthentication(accessToken);
		}

		// 모든 토큰이 유효하지 않을 때
		if (authentication == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		// 유효한 인증 정보 SecurityContext 설정
		SecurityContextHolder.getContext().setAuthentication(authentication);
		filterChain.doFilter(request, response);
	}

	private String getAccessToken(HttpServletRequest request) {
		String token = getAccessTokenFromHeader(request);

		if (token == null) {
			token = cookieManager.getCookieByTokenType(TokenType.ACCESS_TOKEN);
		}

		return token;
	}

	private String getAccessTokenFromHeader(HttpServletRequest request) {
		String token = request.getHeader(AUTHORIZATION_HEADER);

		if (StringUtils.hasText(token) && token.startsWith(BEARER_PREFIX)) {
			return token.substring(BEARER_PREFIX.length());
		}

		return null;
	}

	private Authentication requestGetAuthentication(String accessToken) {
		if (accessToken != null && jwtManager.validate(accessToken)) {
			log.debug("엑세스 토큰이 유효함");
			return jwtManager.getAuthentication(accessToken);
		}
		log.debug("엑세스 토큰이 없거나 만료됨");
		return null;
	}

	private String getAccessTokenByRefreshToken(String refreshToken) {
		//	if (refreshToken != null && jwtManager.validate(refreshToken) && !jwtManager.isLoggedOut(refreshToken)) {
		if (refreshToken != null && jwtManager.validate(refreshToken)) {
			log.debug("리프레쉬 토큰이 유효함");
			return updateNewAccessToken(refreshToken);
		}
		log.debug("리프레쉬 토큰이 없거나 만료됨");
		return null;
	}

	private String updateNewAccessToken(String refreshToken) {
		String[] tokens = jwtManager.recreateTokens(refreshToken);
		String newAccessToken = tokens[0];
		String newRefreshToken = tokens[1];

		cookieManager.setCookie(TokenType.ACCESS_TOKEN, newAccessToken,
			ExpirationPolicy.getAccessTokenExpirationTime());
		log.debug("새로운 엑세스 토큰 발급 완료");

		cookieManager.setCookie(TokenType.REFRESH_TOKEN, newRefreshToken,
			ExpirationPolicy.getRefreshTokenExpirationTime());
		log.debug("새로운 리프레쉬 토큰 발급 완료");

		return newAccessToken;
	}
}
