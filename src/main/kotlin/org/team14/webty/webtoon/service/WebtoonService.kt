package org.team14.webty.webtoon.service

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.team14.webty.webtoon.entity.Webtoon
import org.team14.webty.webtoon.enums.Platform
import org.team14.webty.webtoon.enums.WebtoonSort
import org.team14.webty.webtoon.infrastructure.WebtoonApiClient
import org.team14.webty.webtoon.mapper.WebtoonApiResponseMapper
import org.team14.webty.webtoon.mapper.WebtoonApiResponseMapper.formatAuthors
import org.team14.webty.webtoon.repository.WebtoonRepository

@Service
class WebtoonService(
    private val webtoonRepository: WebtoonRepository,
    private val webtoonApiClient: WebtoonApiClient,
    private val jdbcTemplate: JdbcTemplate
) {
    private val log = LoggerFactory.getLogger(WebtoonService::class.java)
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val DEFAULT_PAGE_SIZE = 100
        private const val DEFAULT_PAGE_NUMBER = 1
        private const val DEFAULT_SORT = "ASC"
        private const val BATCH_SIZE = 500
        private const val PAGE_BATCH_SIZE = 5
    }

    suspend fun saveWebtoons() = coroutineScope {
        Platform.values().map { provider ->
            async { updateWebtoonsByProvider(provider) }
        }.awaitAll()
        log.info("모든 데이터 저장 완료")
    }

    @Transactional
    suspend fun updateWebtoonsByProvider(provider: Platform) {
        val existingWebtoonKeys = webtoonRepository.findExistingWebtoonKeys(provider)
        var page = DEFAULT_PAGE_NUMBER
        var isLastPage = false
        val newWebtoons = mutableListOf<Webtoon>()

        while (!isLastPage) {
            val webtoonResponses = coroutineScope {
                (0 until PAGE_BATCH_SIZE).map {
                    async { webtoonApiClient.getWebtoonPageApiResponse(page + it, DEFAULT_PAGE_SIZE, DEFAULT_SORT, provider) }
                }.awaitAll().filterNotNull().flatMap { it.webtoonApiResponses }
            }

            isLastPage = webtoonResponses.size < (PAGE_BATCH_SIZE * DEFAULT_PAGE_SIZE)

            newWebtoons.addAll(
                webtoonResponses
                    .filter { dto ->
                        val webtoonKey = generateWebtoonKey(dto.title, provider, formatAuthors(dto.authors))
                        !existingWebtoonKeys.contains(webtoonKey)
                    }
                    .map(WebtoonApiResponseMapper::toEntity)
            )

            if (newWebtoons.size >= BATCH_SIZE) {
                batchInsertWebtoons(newWebtoons)
                log.info("새로운 웹툰 {}개 추가 완료 - Provider: {}", newWebtoons.size, provider)
                newWebtoons.clear()
            }

            page += PAGE_BATCH_SIZE
        }

        if (newWebtoons.isNotEmpty()) {
            batchInsertWebtoons(newWebtoons)
            log.info("마지막 배치 저장 완료 - Provider: {}", provider)
        }
    }

    @Transactional
    suspend fun batchInsertWebtoons(webtoons: List<Webtoon>) {
        val sql = "INSERT INTO webtoon (webtoon_name, platform, webtoon_link, thumbnail_url, authors, finished) VALUES (?, ?, ?, ?, ?, ?)"

        webtoons.chunked(BATCH_SIZE).forEach { batch ->
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