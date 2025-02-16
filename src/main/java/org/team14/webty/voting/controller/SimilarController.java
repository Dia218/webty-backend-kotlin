package org.team14.webty.voting.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.team14.webty.common.dto.PageDto;
import org.team14.webty.common.mapper.PageMapper;
import org.team14.webty.security.authentication.WebtyUserDetails;
import org.team14.webty.voting.dto.SimilarRequest;
import org.team14.webty.voting.dto.SimilarResponse;
import org.team14.webty.voting.dto.SimilarResponseRequest;
import org.team14.webty.voting.service.SimilarService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/similar")
public class SimilarController {
	private final SimilarService similarService;

	// 유사 웹툰 등록
	@PostMapping("/create")
	public ResponseEntity<Long> createSimilar(@AuthenticationPrincipal WebtyUserDetails webtyUserDetails,
		@RequestBody SimilarRequest similarRequest) {
		return ResponseEntity.ok().body(similarService.createSimilar(webtyUserDetails, similarRequest.getWebtoonId(),
			similarRequest.getSimilarWebtoonName()));
	}

	// 유사 웹툰 삭제
	@DeleteMapping("/delete/{similarId}")
	public ResponseEntity<Void> deleteSimilar(@AuthenticationPrincipal WebtyUserDetails webtyUserDetails,
		@PathVariable(value = "similarId") Long similarId) {
		similarService.deleteSimilar(webtyUserDetails, similarId);
		return ResponseEntity.ok().build();
	}

	@GetMapping
	public ResponseEntity<PageDto<SimilarResponse>> getSimilarList(
		@RequestParam(value = "page", defaultValue = "0") int page,
		@RequestParam(value = "size", defaultValue = "10") int size,
		@RequestBody SimilarResponseRequest similarResponseRequest) {
		return ResponseEntity.ok()
			.body(PageMapper.toPageDto(similarService.findAll(similarResponseRequest.getWebtoonId(), page, size)));
	}

	@GetMapping("/{similarId}")
	public ResponseEntity<SimilarResponse> getSimilar(@PathVariable(value = "similarId") Long similarId) {
		return ResponseEntity.ok().body(similarService.getSimilar(similarId));
	}
}
