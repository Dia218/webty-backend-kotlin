package org.team14.webty.voting.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.team14.webty.security.authentication.WebtyUserDetails
import org.team14.webty.voting.dto.VoteStatusResponse
import org.team14.webty.voting.service.VoteService

@RestController
@RequestMapping("/vote")
class VoteController(
    private val voteService: VoteService,
) {
    private val logger = KotlinLogging.logger {}

    // 투표
    @PostMapping("/{similarId}")
    fun vote(
        @AuthenticationPrincipal webtyUserDetails: WebtyUserDetails,
        @PathVariable(value = "similarId") similarId: Long,
        @RequestParam(value = "voteType") voteType: String,
        @RequestParam(defaultValue = "0", value = "page") page: Int,
        @RequestParam(defaultValue = "10", value = "size") size: Int,
    ): ResponseEntity<Void> {
        voteService.vote(webtyUserDetails, similarId, voteType, page, size)
        logger.info { "VoteService 투표 실행 로그" }
        return ResponseEntity.ok().build() // 응답은 WebSocket통해서 받아오므로 상태값만 전달
    }

    // 투표 취소
    @DeleteMapping("/{similarId}")
    fun cancel(
        @AuthenticationPrincipal webtyUserDetails: WebtyUserDetails,
        @PathVariable(value = "similarId") similarId: Long,
        @RequestParam(defaultValue = "0", value = "page") page: Int,
        @RequestParam(defaultValue = "10", value = "size") size: Int
    ): ResponseEntity<Void> {
        voteService.cancel(webtyUserDetails, similarId, page, size)
        logger.info { "VoteService 투표 취소 로그" }
        return ResponseEntity.ok().build() // 응답은 WebSocket통해서 받아오므로 상태값만 전달
    }

    // 투표 상태
    @GetMapping("/{similarId}/status")
    fun status(
        @AuthenticationPrincipal webtyUserDetails: WebtyUserDetails,
        @PathVariable(value = "similarId") similarId: Long
    ): ResponseEntity<VoteStatusResponse> {
        val vote = voteService.getVoteStatus(webtyUserDetails, similarId)
        logger.info { "VoteService 투표 상태 조회 로그" }
        return ResponseEntity.ok(VoteStatusResponse(vote?.voteType?.name))
    }
}
