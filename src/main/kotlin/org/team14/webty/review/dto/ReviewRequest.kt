package org.team14.webty.review.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.web.multipart.MultipartFile
import org.team14.webty.review.enumrate.SpoilerStatus

data class ReviewRequest(
    val webtoonId: Long,
    val content: String,
    val title: String,
    val spoilerStatus: SpoilerStatus,
    @JsonIgnore
    var images: List<MultipartFile>? = ArrayList() // 이미지 파일 리스트 추가
)