package org.team14.webty.voting.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.team14.webty.security.authentication.WebtyUserDetails
import org.team14.webty.voting.dto.VoteRequest
import org.team14.webty.voting.service.VoteService

@RestController
@RequestMapping("/vote")
class VoteController(
        private val voteService: VoteService
) {
    // 투표
    @PostMapping
    fun vote(
            @AuthenticationPrincipal webtyUserDetails: WebtyUserDetails,
            @RequestBody voteRequest: VoteRequest
    ): ResponseEntity<Long> {
        return ResponseEntity.ok().body(voteService.vote(webtyUserDetails, voteRequest))
    }

    // 투표 취소
    @DeleteMapping("/{voteId}")
    fun cancel(
            @AuthenticationPrincipal webtyUserDetails: WebtyUserDetails,
            @PathVariable(value = "voteId") voteId: Long
    ): ResponseEntity<Void> {
        voteService.cancel(webtyUserDetails, voteId)
        return ResponseEntity.ok().build()
    }
}
