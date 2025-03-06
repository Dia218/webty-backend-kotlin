package org.team14.webty.voting.listener

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.Channel
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.team14.webty.voting.dto.VoteSuccessEvent

@Component
class VoteSuccessEventListener(
    private val requestChannel: Channel<Unit>
) {

    private val log = LoggerFactory.getLogger(VoteSuccessEventListener::class.java)

    @OptIn(DelicateCoroutinesApi::class)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleVoteSuccessEvent(event: VoteSuccessEvent) {
        log.info("투표 처리 성공, 배치 처리 시작 similarId : {}, userId : {} ", event.similarId, event.userId)
        if (!requestChannel.isClosedForSend) {
            requestChannel.trySend(Unit)
        }
    }
}