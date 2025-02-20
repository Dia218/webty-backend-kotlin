package org.team14.webty.webtoon.dto


data class WebtoonSummaryDto(
        val webtoonName: String,
        val webtoonId: Long?,
        val thumbnailUrl: String
)