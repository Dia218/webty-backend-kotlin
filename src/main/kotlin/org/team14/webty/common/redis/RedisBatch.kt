package org.team14.webty.common.redis

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.team14.webty.voting.service.VoteService

@Component
class RedisBatch(
    private val voteService: VoteService,
    private val requestChannel: Channel<Unit> // 비동기 메세지 큐 역할
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val logger = KotlinLogging.logger {}
    private val mutex = Mutex() // 동시 접근 방지 (여러 코루틴 실행 X)

    @PostConstruct
    fun startProcessing() {
        scope.launch {
            while (isActive) {
                // 최소 하나의 요청을 받을 때까지 대기 (메시지 소비)
                requestChannel.receive()

                val startTime = System.currentTimeMillis()
                // 추가 요청이 있는지 100ms 동안 대기 (추가 요청은 무시하고 대기 시간만 확보)
                while (System.currentTimeMillis() - startTime < 100) {
                    delay(10)
                    // 추가 요청이 있다면 소비하여 버퍼를 비워줌 (채널이 비어있을때까지)
                    while (requestChannel.tryReceive().isSuccess) {
                    }
                }
                // 100ms 대기 후, 무조건 배치 처리 실행
                mutex.withLock {
                    logger.info { "변경 사항 감지됨, 배치 처리 실행" }
                    voteService.publish(PageRequest.of(0, 10))
                }
            }
        }
    }
}





