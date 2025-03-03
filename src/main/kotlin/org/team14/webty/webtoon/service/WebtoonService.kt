package org.team14.webty.webtoon.service

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.team14.webty.webtoon.api.WebtoonApiResponse
import org.team14.webty.webtoon.api.WebtoonPageApiResponse
import org.team14.webty.webtoon.entity.Webtoon
import org.team14.webty.webtoon.enums.Platform
import org.team14.webty.webtoon.enums.WebtoonSort
import org.team14.webty.webtoon.mapper.WebtoonApiResponseMapper
import org.team14.webty.webtoon.mapper.WebtoonApiResponseMapper.formatAuthors
import org.team14.webty.webtoon.repository.WebtoonRepository
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Service
class WebtoonService(
    private val webtoonRepository: WebtoonRepository,
    private val restTemplate: RestTemplate,
    private val jdbcTemplate: JdbcTemplate
) {
    private val log = LoggerFactory.getLogger(WebtoonService::class.java)
    private val executor: ExecutorService = Executors.newFixedThreadPool(5)

    companion object {
        private const val URL_QUERY_TEMPLATE =
            "https://korea-webtoon-api-cc7dda2f0d77.herokuapp.com/webtoons?page=%s&perPage=%s&sort=%s&provider=%s"
        private const val DEFAULT_PAGE_SIZE = 100
        private const val DEFAULT_PAGE_NUMBER = 1
        private const val DEFAULT_SORT = "ASC"
        private const val BATCH_SIZE = 500
        private const val PAGE_BATCH_SIZE = 5
    }

    fun saveWebtoons() {
        val futures = Platform.values().map { provider ->
            CompletableFuture.runAsync({ saveWebtoonsByProvider(provider) }, executor)
        }
        CompletableFuture.allOf(*futures.toTypedArray()).join()
        log.info("모든 데이터 저장 완료")
    }

    @Transactional
    fun saveWebtoonsByProvider(provider: Platform) {
        var page = DEFAULT_PAGE_NUMBER
        var isLastPage = false

        while (!isLastPage) {
            val webtoonPageApiResponse = getWebtoonPageApiResponse(page, DEFAULT_PAGE_SIZE, DEFAULT_SORT, provider)
                ?: break

            val webtoons = webtoonPageApiResponse.webtoonApiResponses
                .map(WebtoonApiResponseMapper::toEntity)

            batchInsertWebtoons(webtoons)

            isLastPage = webtoonPageApiResponse.isLastPage
            page++
        }
    }

    @Scheduled(cron = "50 52 19 * * ?", zone = "Asia/Seoul")
    fun updateWebtoons() {
        log.info("웹툰 데이터 업데이트 시작 (비동기)")
        val futures = Platform.values().map { provider ->
            CompletableFuture.runAsync({ updateWebtoonsByProvider(provider) }, executor)
        }
        CompletableFuture.allOf(*futures.toTypedArray()).join()
        log.info("웹툰 데이터 업데이트 요청 완료")
    }

    @Transactional
    fun updateWebtoonsByProvider(provider: Platform) {
        val existingWebtoonKeys = webtoonRepository.findAll()
            .map { generateWebtoonKey(it) }
            .toSet()

        var page = DEFAULT_PAGE_NUMBER
        var isLastPage = false

        while (!isLastPage) {
            val webtoonResponses = mutableListOf<WebtoonApiResponse>()

            repeat(PAGE_BATCH_SIZE) {
                val webtoonPageApiResponse = getWebtoonPageApiResponse(page, DEFAULT_PAGE_SIZE, DEFAULT_SORT, provider)
                    ?: return@repeat  // API 응답이 없으면 반복문 탈출

                webtoonResponses.addAll(webtoonPageApiResponse.webtoonApiResponses)

                isLastPage = webtoonPageApiResponse.isLastPage
                page++
                if (isLastPage) return@repeat
            }

            val newWebtoons = webtoonResponses
                .filter { dto ->
                    val webtoonKey = generateWebtoonKey(dto.title, provider, formatAuthors(dto.authors))
                    !existingWebtoonKeys.contains(webtoonKey)
                }
                .map(WebtoonApiResponseMapper::toEntity)

            if (newWebtoons.isNotEmpty()) {
                batchInsertWebtoons(newWebtoons)
                log.info("새로운 웹툰 {}개 추가 완료 - Provider: {}", newWebtoons.size, provider)
            }
        }
    }

    @Transactional
    fun batchInsertWebtoons(webtoons: List<Webtoon>) {
        val sql = "INSERT INTO webtoon (webtoon_name, platform, webtoon_link, thumbnail_url, authors, finished) VALUES (?, ?, ?, ?, ?, ?)"

        webtoons.chunked(500).forEach { batch ->
            jdbcTemplate.batchUpdate(sql, batch.map { webtoon ->
                arrayOf(
                    webtoon.webtoonName,
                    webtoon.platform.name,
                    webtoon.webtoonLink,
                    webtoon.thumbnailUrl,
                    webtoon.authors,
                    webtoon.finished
                )
            })
        }
    }

    private fun getWebtoonPageApiResponse(
        page: Int, perPage: Int, sort: String, provider: Platform
    ): WebtoonPageApiResponse? {
        val url = String.format(URL_QUERY_TEMPLATE, page, perPage, sort, provider.platformName)
        return try {
            restTemplate.getForObject(url, WebtoonPageApiResponse::class.java)
        } catch (e: RestClientException) {
            log.error("API 요청 실패 - URL: {}, Error: {}", url, e.message, e)
            null
        }
    }

    private fun generateWebtoonKey(webtoon: Webtoon): String {
        return "${webtoon.webtoonName}|${webtoon.platform.name}|${webtoon.authors}"
    }

    private fun generateWebtoonKey(title: String, platform: Platform, authors: String): String {
        return "$title|${platform.name}|$authors"
    }

    fun findWebtoon(id: Long): Webtoon {
        return webtoonRepository.findById(id)
            .orElseThrow {
                IllegalArgumentException("웹툰을 찾을 수 없습니다. id: $id")
            }
    }

    fun searchWebtoons(
        webtoonName: String?,
        platform: Platform?,
        authors: String?,
        finished: Boolean?,
        page: Int,
        size: Int,
        sortBy: String,
        sortDirection: String
    ): Page<Webtoon> {
        val direction = if (sortDirection.equals("desc", ignoreCase = true)) Sort.Direction.DESC else Sort.Direction.ASC
        val sortField = WebtoonSort.fromString(sortBy)?.field ?: WebtoonSort.WEBTOON_NAME.field
        val pageable: Pageable = PageRequest.of(page, size, Sort.by(direction, sortField))
        return webtoonRepository.searchWebtoons(webtoonName, platform, authors, finished, pageable)
    }
}