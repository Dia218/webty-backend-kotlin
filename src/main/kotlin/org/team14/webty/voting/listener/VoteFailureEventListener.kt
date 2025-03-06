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

@Component
class VoteFailureEventListener(
    private val voteCacheService: VoteCacheService
) {

    private val log = LoggerFactory.getLogger(VoteFailureEventListener::class.java)

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    fun handleVoteFailureEvent(event: VoteFailureEvent) {
        log.info("투표 처리 실패, 트랜잭션 롤백 similarId : {}, userId : {} ", event.similarId, event.userId)
        voteCacheService.deleteUserVote(event.similarId, event.userId)
    }
}