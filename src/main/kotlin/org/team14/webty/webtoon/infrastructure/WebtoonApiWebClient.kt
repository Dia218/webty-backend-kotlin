package org.team14.webty.webtoon.infrastructure

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.team14.webty.webtoon.api.WebtoonPageApiResponse
import org.team14.webty.webtoon.enums.Platform
import org.team14.webty.webtoon.service.WebtoonService

@Component
class WebtoonApiClient(private val webClient: WebClient) {
    companion object {
        private const val URL_QUERY_TEMPLATE =
            "https://korea-webtoon-api-cc7dda2f0d77.herokuapp.com/webtoons?page=%s&perPage=%s&sort=%s&provider=%s"
    }
    private val log = LoggerFactory.getLogger(WebtoonService::class.java)

    suspend fun getWebtoonPageApiResponse(
        page: Int, perPage: Int, sort: String, provider: Platform
    ): WebtoonPageApiResponse? {
        val url = String.format(URL_QUERY_TEMPLATE, page, perPage, sort, provider.platformName)
        return try {
            webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(WebtoonPageApiResponse::class.java)
                .awaitSingleOrNull()
        } catch (e: Exception) {
            log.error("API 요청 실패 - URL: {}, Error: {}", url, e.message, e)
            null
        }
    }
}