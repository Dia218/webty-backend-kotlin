package org.team14.webty.search.mapper

import org.springframework.stereotype.Component
import org.team14.webty.recommend.repository.RecommendRepository

@Component
class SearchRecommendMapper(
    private val recommendRepository: RecommendRepository
) {
    /**
     * 리뷰 ID 목록에 대한 좋아요 수를 조회합니다.
     * 
     * @param reviewIds 좋아요 수를 조회할 리뷰 ID 리스트
     * @return 리뷰 ID를 키로, 좋아요 수를 값으로 하는 맵
     */
    fun getLikesCounts(reviewIds: List<Long>): Map<Long, Int> {
        if (reviewIds.isEmpty()) {
            return emptyMap()
        }
        
        return reviewIds.associateWith { reviewId ->
            val counts = recommendRepository.getRecommendCounts(reviewId)
            counts["likes"]?.toInt() ?: 0
        }
    }
} 