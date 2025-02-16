package org.team14.webty.webtoon.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.team14.webty.common.dto.PageDto;
import org.team14.webty.common.mapper.PageMapper;
import org.team14.webty.webtoon.dto.WebtoonDetailDto;
import org.team14.webty.webtoon.dto.WebtoonSearchRequest;
import org.team14.webty.webtoon.entity.Webtoon;
import org.team14.webty.webtoon.enumerate.Platform;
import org.team14.webty.webtoon.mapper.WebtoonDetailMapper;
import org.team14.webty.webtoon.service.WebtoonService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webtoons")
@Slf4j
public class WebtoonController {
	private final WebtoonService webtoonService;

	// @GetMapping("/fetch") // 초기화 할 때만 사용
	// public void fetchWebtoons() {
	// 	webtoonService.saveWebtoons();
	// }

	@GetMapping("/{id}")
	public ResponseEntity<WebtoonDetailDto> findWebtoon(@Valid @Min(1) @PathVariable(value = "id") Long id) {
		Webtoon webtoon = webtoonService.findWebtoon(id);
		return ResponseEntity.ok(WebtoonDetailMapper.toDto(webtoon));
	}

	@GetMapping
	public ResponseEntity<PageDto<WebtoonDetailDto>> searchWebtoons(@Valid WebtoonSearchRequest request) {
		log.info(request.getPlatform());
		Page<WebtoonDetailDto> webtoons = webtoonService.searchWebtoons(
			request.getWebtoonName(),
			request.getPlatform() != null && !request.getPlatform().isEmpty() ?
				Platform.fromString(request.getPlatform()) : null,
			request.getAuthors(),
			request.getFinished(),
			request.getPage(),
			request.getSize(),
			request.getSortBy(),
			request.getSortDirection()).map(WebtoonDetailMapper::toDto);
		return ResponseEntity.ok(PageMapper.toPageDto(webtoons));
	}
}
