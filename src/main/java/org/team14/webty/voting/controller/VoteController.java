package org.team14.webty.voting.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.team14.webty.security.authentication.WebtyUserDetails;
import org.team14.webty.voting.dto.VoteRequest;
import org.team14.webty.voting.service.VoteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vote")
public class VoteController {
	private final VoteService voteService;

	// 투표
	@PostMapping
	public ResponseEntity<Long> vote(@AuthenticationPrincipal WebtyUserDetails webtyUserDetails,
		@RequestBody VoteRequest voteRequest) {
		return ResponseEntity.ok().body(voteService.vote(webtyUserDetails, voteRequest));
	}

	// 투표 취소
	@DeleteMapping("{voteId}")
	public ResponseEntity<Void> cancel(@AuthenticationPrincipal WebtyUserDetails webtyUserDetails,
		@PathVariable(value = "voteId") Long voteId) {
		voteService.cancel(webtyUserDetails, voteId);
		return ResponseEntity.ok().build();
	}
}
