package org.team14.webty.webtoon.service

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.team14.webty.webtoon.api.WebtoonPageApiResponse
import org.team14.webty.webtoon.entity.Webtoon
import org.team14.webty.webtoon.enumerate.Platform
import org.team14.webty.webtoon.enumerate.WebtoonSort
import org.team14.webty.webtoon.mapper.WebtoonApiResponseMapper
import org.team14.webty.webtoon.mapper.WebtoonApiResponseMapper.formatAuthors
import org.team14.webty.webtoon.repository.WebtoonRepository

@Service
class WebtoonService(
    private val webtoonRepository: WebtoonRepository,
    private val restTemplate: RestTemplate
) {
    private val log = LoggerFactory.getLogger(WebtoonService::class.java)
    
    companion object {
        private const val URL_QUERY_TEMPLATE =
            "https://korea-webtoon-api-cc7dda2f0d77.herokuapp.com/webtoons?page=%s&perPage=%s&sort=%s&provider=%s"
        private const val DEFAULT_PAGE_SIZE = 100
        private const val DEFAULT_PAGE_NUMBER = 1
        private const val DEFAULT_SORT = "ASC"
    }
    
    @Transactional
    fun saveWebtoons() {
        Platform.values().forEach { provider ->
            try {
                saveWebtoonsByProvider(provider)
            } catch (e: Exception) {
                log.error("웹툰 저장 중 오류 발생 - Provider: {}, Error: {}", provider, e.message, e)
            }
        }
        log.info("모든 데이터 저장 완료")
    }
    
    private fun saveWebtoonsByProvider(provider: Platform) {
        var isLastPage: Boolean
        var page = DEFAULT_PAGE_NUMBER
        
        do {
            log.info(page.toString())
            val webtoonPageApiResponse = getWebtoonPageApiResponse(page, DEFAULT_PAGE_SIZE, DEFAULT_SORT, provider)
            webtoonPageApiResponse?.run { saveWebtoonsFromPage(this) }
            isLastPage = webtoonPageApiResponse?.isLastPage ?: true
            page++
        } while (!isLastPage)
    }
    
    private fun getWebtoonPageApiResponse(
        page: Int,
        perPage: Int,
        sort: String,
        provider: Platform
    ): WebtoonPageApiResponse? {
        val url = String.format(URL_QUERY_TEMPLATE, page, perPage, sort, provider.platformName)
        log.info(url)
        return try {
            restTemplate.getForObject(url, WebtoonPageApiResponse::class.java)
        } catch (e: RestClientException) {
            log.error("API 요청 실패 - URL: {}, Error: {}", url, e.message, e)
            null
        }
    }
    
    private fun saveWebtoonsFromPage(webtoonPageApiResponse: WebtoonPageApiResponse) {
        val webtoons = webtoonPageApiResponse.webtoonApiResponses
            .map(WebtoonApiResponseMapper::toEntity)
        webtoonRepository.saveAll(webtoons)
    }
    
    @Scheduled(cron = "0 0 6 * * ?", zone = "Asia/Seoul")
    fun updateWebtoons() {
        log.info("웹툰 데이터 업데이트 시작 (비동기)")
        Platform.values().forEach { updateWebtoonsByProviderAsync(it) }
        log.info("웹툰 데이터 업데이트 요청 완료")
    }
    
    @Async
    @Transactional
    fun updateWebtoonsByProviderAsync(provider: Platform) {
        try {
            val existingWebtoonKeys = webtoonRepository.findAll()
                .map { generateWebtoonKey(it) }
                .toSet()
            updateWebtoonsByProvider(provider, existingWebtoonKeys)
        } catch (e: Exception) {
            log.error("웹툰 업데이트 중 오류 발생 - Provider: {}, Error: {}", provider, e.message, e)
        }
    }
    
    private fun updateWebtoonsByProvider(provider: Platform, existingWebtoonKeys: Set<String>) {
        var isLastPage: Boolean
        var page = DEFAULT_PAGE_NUMBER
        
        do {
            val webtoonPageApiResponse = getWebtoonPageApiResponse(page, DEFAULT_PAGE_SIZE, DEFAULT_SORT, provider)
            
            if (webtoonPageApiResponse?.webtoonApiResponses.isNullOrEmpty()) {
                log.warn("응답이 없습니다. - Provider: {}, Page: {}", provider, page)
                break
            }
            
            val newWebtoons = webtoonPageApiResponse!!.webtoonApiResponses
                .filter { dto ->
                    val webtoonKey = generateWebtoonKey(dto.title, provider, formatAuthors(dto.authors))
                    !existingWebtoonKeys.contains(webtoonKey)
                }
                .map(WebtoonApiResponseMapper::toEntity)
            
            if (newWebtoons.isNotEmpty()) {
                webtoonRepository.saveAll(newWebtoons)
                log.info("새로운 웹툰 {}개 추가 완료 - Provider: {}", newWebtoons.size, provider)
            } else {
                log.info("Provider: {} - 추가할 새로운 웹툰이 없습니다.", provider)
            }
            
            isLastPage = webtoonPageApiResponse.isLastPage
            page++
        } while (!isLastPage)
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