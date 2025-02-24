package org.team14.webty.webtoon.mapper

import org.team14.webty.webtoon.api.WebtoonApiResponse
import org.team14.webty.webtoon.dto.WebtoonDetailDto
import org.team14.webty.webtoon.dto.WebtoonSummaryDto
import org.team14.webty.webtoon.entity.Webtoon
import org.team14.webty.webtoon.enums.Platform


object WebtoonApiResponseMapper {
    fun toEntity(webtoonApiResponse: WebtoonApiResponse): Webtoon {
        return Webtoon(
            webtoonName = webtoonApiResponse.title,
            platform = Platform.fromString(webtoonApiResponse.provider),
            webtoonLink = webtoonApiResponse.url,
            thumbnailUrl = webtoonApiResponse.thumbnails[0],
            authors = formatAuthors(webtoonApiResponse.authors),
            finished = webtoonApiResponse.isEnd
        )
    }

    fun toDto(webtoon: Webtoon): WebtoonDetailDto {
        return WebtoonDetailDto(
            webtoonId = webtoon.webtoonId,
            webtoonName = webtoon.webtoonName,
            platform = webtoon.platform,
            webtoonLink = webtoon.webtoonLink,
            thumbnailUrl = webtoon.thumbnailUrl,
            authors = webtoon.authors,
            finished = webtoon.finished
        )
    }

    fun toSummaryDto(webtoon: Webtoon): WebtoonSummaryDto {
        return WebtoonSummaryDto(
            webtoonId = webtoon.webtoonId,
            webtoonName = webtoon.webtoonName,
            thumbnailUrl = webtoon.thumbnailUrl
        )
    }

    fun formatAuthors(authors: List<String>?): String {
        return authors?.joinToString(", ") ?: ""
    }
}