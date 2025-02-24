package org.team14.webty.voting.controller

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.team14.webty.common.exception.BusinessException
import org.team14.webty.common.exception.ErrorCode
import org.team14.webty.security.authentication.WebtyUserDetails
import org.team14.webty.voting.dto.VoteRequest
import org.team14.webty.voting.service.VoteService

@Controller
class WebSocketController(
    private val simpMessagingTemplate: SimpMessagingTemplate,
    private val voteService: VoteService
) { // SimpMessagingTemplate 주입

    data class VotingResponse(val similarId: Long, val result: Long, val user: String)

    @MessageMapping("/vote")
    fun receiveVote(
        @Payload voteRequest: VoteRequest,
        headerAccessor: SimpMessageHeaderAccessor
    ) {
        val sessionAttributes = headerAccessor.sessionAttributes
        val webtyUserDetails = sessionAttributes?.get("user") as? WebtyUserDetails
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
        val result = voteService.vote(webtyUserDetails, voteRequest)

        println("VoteService 실행 완료 - similarId: ${voteRequest.similarId}, 현재 점수: $result") // VoteService 실행 로그

        val response = VotingResponse(voteRequest.similarId, result, webtyUserDetails.webtyUser.nickname)

        // 동적으로 채널로 메시지 전송
        val destination = "/topic/vote-results/${voteRequest.similarId}"
        simpMessagingTemplate.convertAndSend(destination, response) // 채널로 메시지 전송
        println("WebSocket 메시지 전송 완료 - destination: $destination") // 메시지 전송 로그
    }
}

