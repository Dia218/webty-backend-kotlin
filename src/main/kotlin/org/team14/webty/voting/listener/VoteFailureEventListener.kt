package org.team14.webty.voting.listener

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.team14.webty.voting.cache.VoteCacheService
import org.team14.webty.voting.dto.VoteFailureEvent
import org.team14.webty.voting.enums.VoteFailureType

@Component
class VoteFailureEventListener(
    private val voteCacheService: VoteCacheService
) {

    private val log = LoggerFactory.getLogger(VoteFailureEventListener::class.java)

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    fun handleVoteFailureEvent(event: VoteFailureEvent) {
        log.info(
            "투표 처리 실패, 트랜잭션 롤백 similarId : {}, userId : {} type: {}",
            event.similarId,
            event.userId,
            event.voteFailureType
        )

        when (event.voteFailureType) {
            // 투표 요청 실패 시 redis 유저 투표 내역 삭제
            VoteFailureType.VOTE_FAILURE -> {
                voteCacheService.deleteUserVote(event.similarId, event.userId)
            }
            // 투표 취소 실패 시 삭제된 투표 내역을 복구
            VoteFailureType.VOTE_CANCEL_FAILURE -> {
                voteCacheService.setUserVote(event.similarId, event.userId)
            }
        }
    }
}