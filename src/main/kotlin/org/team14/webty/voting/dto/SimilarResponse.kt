package org.team14.webty.voting.dto

class SimilarResponse(
    private val similarId: Long,
    private val similarThumbnailUrl: String,
    private val similarResult: Long,
    private val similarWebtoonId: Long // webtoon-detail 페이지 이동 시 필요
)
