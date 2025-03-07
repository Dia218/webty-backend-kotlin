package org.team14.webty.webtoon.schedueler

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.team14.webty.webtoon.enums.Platform
import org.team14.webty.webtoon.service.WebtoonService

@Component
class WebtoonScheduler(
    private val webtoonService: WebtoonService
) {
    private val log = LoggerFactory.getLogger(WebtoonScheduler::class.java)


    @Scheduled(cron = "0 0 6 * * ?", zone = "Asia/Seoul")
    fun updateWebtoons() {
        runBlocking {
            log.info("웹툰 데이터 업데이트 시작 (비동기)")
            val start = System.currentTimeMillis()
            Platform.values().map { provider ->
                async { webtoonService.updateWebtoonsByProvider(provider) }
            }.awaitAll()
            val end = System.currentTimeMillis()
            log.info("웹툰 데이터 업데이트 요청 완료 - 총 소요 시간: {} ms", (end - start))
        }
    }
}