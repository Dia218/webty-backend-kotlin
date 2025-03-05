package org.team14.webty.voting.mapper

import org.team14.webty.voting.dto.SimilarResponse
import org.team14.webty.voting.entity.Similar
import org.team14.webty.webtoon.entity.Webtoon

object SimilarMapper {
    fun toEntity(userId: Long, choiceWebtoonId: Long, targetWebtoon: Webtoon): Similar {
        return Similar(
            similarWebtoonId = choiceWebtoonId,
            similarResult = 0L,
            userId = userId,
            targetWebtoon = targetWebtoon
        )
    }

    fun toResponse(similar: Similar, similarWebtoon: Webtoon, agreeCount: Long, disagreeCount: Long): SimilarResponse {
        return SimilarResponse(
            similarId = similar.similarId!!,
            targetWebtoonId = similar.targetWebtoon.webtoonId!!,
            similarWebtoonName = similarWebtoon.webtoonName,
            similarThumbnailUrl = similarWebtoon.thumbnailUrl,
            similarResult = similar.similarResult,
            similarWebtoonId = similarWebtoon.webtoonId!!,
            agreeCount = agreeCount,
            disagreeCount = disagreeCount
        )
    }
}
