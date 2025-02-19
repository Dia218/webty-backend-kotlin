package org.team14.webty.webtoon.dto

import org.team14.webty.webtoon.enumerate.Platform


data class WebtoonDetailDto(
    val webtoonId: Long?,
    val webtoonName: String,
    val platform: Platform,
    val webtoonLink: String,
    val thumbnailUrl: String,
    val authors: String,
    val finished: Boolean
)

