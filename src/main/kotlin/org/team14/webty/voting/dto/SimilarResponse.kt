package org.team14.webty.voting.dto

data class SimilarResponse(
    val similarId: Long,
    val targetWebtoonId: Long,
    val similarWebtoonName: String,
    val similarThumbnailUrl: String,
    val similarResult: Long,
    val similarWebtoonId: Long, // webtoon-detail 페이지 이동 시 필요
    val agreeCount: Long,
    val disagreeCount: Long
)
