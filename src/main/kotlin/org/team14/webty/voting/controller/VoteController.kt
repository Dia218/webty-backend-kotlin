package org.team14.webty.voting.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.team14.webty.common.mapper.PageMapper
import org.team14.webty.common.redis.RedisPublisher
import org.team14.webty.security.authentication.WebtyUserDetails
import org.team14.webty.voting.dto.VoteRequest
import org.team14.webty.voting.service.VoteService

@RestController
@RequestMapping("/vote")
class VoteController(
    private val voteService: VoteService,
    private val redisPublisher: RedisPublisher
) {
    private val logger = KotlinLogging.logger {}

    // 투표
    @PostMapping
    fun vote(
        @AuthenticationPrincipal webtyUserDetails: WebtyUserDetails,
        @RequestBody voteRequest: VoteRequest,
        @RequestParam(defaultValue = "0", value = "page") page: Int,
        @RequestParam(defaultValue = "10", value = "size") size: Int,
    ): ResponseEntity<Void> {
        val response =
            voteService.vote(webtyUserDetails, voteRequest, page, size)
        val responsePageDto =
            PageMapper.toPageDto(response) // to do: voteService 에서 PageDto<SimilarResponse> 를 반환하도록 수정
        logger.info { "VoteService 실행 로그" }

        redisPublisher.publish("vote-results", responsePageDto)

        return ResponseEntity.ok().build() // 응답은 WebSocket통해서 받아오므로 상태값만 전달
    }

    // 투표 취소 // to do: 웹소켓으로 변경
    @DeleteMapping("/{voteId}")
    fun cancel(
        @AuthenticationPrincipal webtyUserDetails: WebtyUserDetails,
        @PathVariable(value = "voteId") voteId: Long
    ): ResponseEntity<Void> {
        voteService.cancel(webtyUserDetails, voteId)
        return ResponseEntity.ok().build()
    }
}
