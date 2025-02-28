package org.team14.webty.common.redis

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.team14.webty.voting.service.VoteService
import java.util.concurrent.atomic.AtomicBoolean

@Component
class RedisBatch(
    private val voteService: VoteService
) {
    private val scope = CoroutineScope(Dispatchers.IO) // IO 최적화
    private val isProcessing = AtomicBoolean(false) // 중복 실행 방지
    private val logger = KotlinLogging.logger {}

    @PostConstruct
    fun startProcessing() {
        voteService.onChangeCallback = {
            if (isProcessing.compareAndSet(false, true)) { // 실행 중이 아니라면 실행
                scope.launch {
                    delay(100) // 100ms 동안 요청을 모은 후 실행
                    logger.info { "변경 사항 감지됨, 배치 처리 실행" }
                    val pageable: Pageable = PageRequest.of(0, 10)
                    voteService.publish(pageable)
                    isProcessing.set(false) // 처리 완료 후 다시 false로 설정
                }
            }
        }
    }
}
