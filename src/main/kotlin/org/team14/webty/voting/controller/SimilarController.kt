package org.team14.webty.voting.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.team14.webty.common.dto.PageDto
import org.team14.webty.common.mapper.PageMapper
import org.team14.webty.security.authentication.WebtyUserDetails
import org.team14.webty.voting.dto.SimilarRequest
import org.team14.webty.voting.dto.SimilarResponse
import org.team14.webty.voting.service.SimilarService

@RestController
@RequestMapping("/similar")
class SimilarController(
        private val similarService: SimilarService
) {
    // 유사 웹툰 등록
    @PostMapping
    fun createSimilar(
            @AuthenticationPrincipal webtyUserDetails: WebtyUserDetails,
            @RequestBody similarRequest: SimilarRequest
    ): ResponseEntity<SimilarResponse> {
        return ResponseEntity.ok()
                .body(
                        similarService.createSimilar(
                                webtyUserDetails,
                                similarRequest.targetWebtoonId,
                                similarRequest.choiceWebtoonId
                        )
                )
    }

    // 유사 웹툰 삭제
    @DeleteMapping("/{similarId}")
    fun deleteSimilar(
            @AuthenticationPrincipal webtyUserDetails: WebtyUserDetails,
            @PathVariable(value = "similarId") similarId: Long
    ): ResponseEntity<Void> {
        similarService.deleteSimilar(webtyUserDetails, similarId)
        return ResponseEntity.ok().build()
    }

    // 유사 웹툰 목록 조회
    @GetMapping
    fun getSimilarList(
            @RequestParam(value = "page", defaultValue = "0") page: Int,
            @RequestParam(value = "size", defaultValue = "10") size: Int,
            @RequestParam("targetWebtoonId") targetWebtoonId: Long
    ): ResponseEntity<PageDto<SimilarResponse>> {
        return ResponseEntity.ok()
                .body(
                        PageMapper.toPageDto(similarService.findAll(targetWebtoonId, page, size))
                )
    }
}
