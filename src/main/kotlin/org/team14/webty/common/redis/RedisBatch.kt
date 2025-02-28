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
            while (isActive) { // 실행 중이면 계속 대기
                requestChannel.receive() // 최소 하나의 요청을 받을 때까지 대기

                val startTime = System.currentTimeMillis() // 시작 시간 기록
                var hasNewRequest = false

                while (System.currentTimeMillis() - startTime < 100) { // 100ms 동안 추가 요청 감지
                    delay(10) // 10ms마다 확인

                    while (requestChannel.tryReceive().isSuccess) {
                        hasNewRequest = true // 새로운 요청이 있으면 True 설정
                    }
                }

                if (hasNewRequest) { // 새로운 요청이 있었다면 실행
                    mutex.withLock {
                        logger.info { "변경 사항 감지됨, 배치 처리 실행" }
                        voteService.publish(PageRequest.of(0, 10)) // 한 번만 실행
                    }
                }
            }
        }
    }
}





