package org.team14.webty.webtoon.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.team14.webty.security.authentication.WebtyUserDetails;
import org.team14.webty.webtoon.dto.WebtoonDetailDto;
import org.team14.webty.webtoon.service.FavoriteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
@Slf4j
public class FavoriteController {

	private final FavoriteService favoriteService;

	@PostMapping("/{webtoonId}")
	public ResponseEntity<Void> add(@AuthenticationPrincipal WebtyUserDetails webtyUserDetails,
		@PathVariable(value = "webtoonId") Long webtoonId) {
		favoriteService.addFavorite(webtyUserDetails, webtoonId);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{webtoonId}")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal WebtyUserDetails webtyUserDetails,
		@PathVariable(value = "webtoonId") Long webtoonId) {
		favoriteService.deleteFavorite(webtyUserDetails, webtoonId);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/list")
	public ResponseEntity<List<WebtoonDetailDto>> getUserFavorite(
		@AuthenticationPrincipal WebtyUserDetails webtyUserDetails) {
		List<WebtoonDetailDto> userFavorites = favoriteService.getUserFavorites(webtyUserDetails);
		return ResponseEntity.ok().body(userFavorites);
	}

	@GetMapping("/{webtoonId}")
	public ResponseEntity<Boolean> checkFavorite(@AuthenticationPrincipal WebtyUserDetails webtyUserDetails,
		@PathVariable(value = "webtoonId") Long webtoonId) {
		boolean isFavorite = favoriteService.checkFavoriteWebtoon(webtyUserDetails, webtoonId);
		return ResponseEntity.ok().body(isFavorite);
	}
}
