package org.team14.webty.voting.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.team14.webty.common.exception.BusinessException
import org.team14.webty.common.exception.ErrorCode
import org.team14.webty.common.mapper.PageMapper
import org.team14.webty.common.redis.RedisPublisher
import org.team14.webty.security.authentication.WebtyUserDetails
import org.team14.webty.voting.dto.VoteRequest
import org.team14.webty.voting.service.VoteService

@Controller
class VoteController(
    private val voteService: VoteService,
    private val redisPublisher: RedisPublisher
) {
    private val logger = KotlinLogging.logger {}

    // 투표
    @MessageMapping("/vote")
    fun vote(
        @Payload voteRequest: VoteRequest,
        headerAccessor: SimpMessageHeaderAccessor
    ) {
        val webtyUserDetails = headerAccessor.sessionAttributes?.get("user") as? WebtyUserDetails
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
        val response =
            voteService.vote(webtyUserDetails, voteRequest)
        val responsePageDto =
            PageMapper.toPageDto(response) // to do: voteService 에서 PageDto<SimilarResponse> 를 반환하도록 수정
        logger.info { "VoteService 실행 로그" }

        redisPublisher.publish("vote-results", responsePageDto)
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
