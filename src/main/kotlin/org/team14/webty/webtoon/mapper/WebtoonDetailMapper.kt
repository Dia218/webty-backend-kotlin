package org.team14.webty.webtoon.mapper

import org.team14.webty.webtoon.dto.WebtoonDetailDto
import org.team14.webty.webtoon.entity.Webtoon

object WebtoonDetailMapper {
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
}