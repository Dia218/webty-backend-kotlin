package org.team14.webty.webtoon.controller

import WebtoonSearchRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.team14.webty.common.dto.PageDto
import org.team14.webty.common.mapper.PageMapper
import org.team14.webty.webtoon.dto.WebtoonDetailDto
import org.team14.webty.webtoon.enums.Platform
import org.team14.webty.webtoon.mapper.WebtoonDetailMapper
import org.team14.webty.webtoon.service.WebtoonService

@RestController
@RequestMapping("/webtoons")
class WebtoonController(
    private val webtoonService: WebtoonService
) {
    @GetMapping("/fetch") // 초기화 할 때만 사용
    fun fetchWebtoons() {
        webtoonService.saveWebtoons()
    }

    @GetMapping("/{id}")
    fun findWebtoon(@PathVariable("id") @Min(1) id: Long): ResponseEntity<WebtoonDetailDto> {
        val webtoon = webtoonService.findWebtoon(id)
        return ResponseEntity.ok(WebtoonDetailMapper.toDto(webtoon))
    }

    @GetMapping
    fun searchWebtoons(@Valid request: WebtoonSearchRequest): ResponseEntity<PageDto<WebtoonDetailDto>> {
        val webtoons = webtoonService.searchWebtoons(
            request.webtoonName,
            if (!request.platform.isNullOrEmpty()) Platform.fromString(request.platform) else null,
            request.authors,
            request.finished,
            request.page,
            request.size,
            request.sortBy,
            request.sortDirection
        ).map(WebtoonDetailMapper::toDto)
        return ResponseEntity.ok(PageMapper.toPageDto(webtoons))
    }
}