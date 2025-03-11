package org.team14.webty.review.cache

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.team14.webty.review.repository.ReviewRepository

@Service
class ViewCountCacheService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val reviewRepository: ReviewRepository
) {
    // Redis 키 패턴
    companion object {
        private const val VIEW_COUNT_KEY_PREFIX = "review:viewCount:"
    }

    /**
     * 조회수 증가 메서드
     * 조회가 발생할 때마다 Redis의 카운터 증가
     */
    fun incrementViewCount(reviewId: Long) {
        redisTemplate.opsForValue().increment(VIEW_COUNT_KEY_PREFIX + reviewId)
    }

    /**
     * 현재 조회수 조회 메서드
     * 캐시된 조회수 + DB 조회수 합산 반환
     */
    fun getCurrentViewCount(reviewId: Long, dbViewCount: Int): Int {
        val cachedCount =
            redisTemplate.opsForValue().get(VIEW_COUNT_KEY_PREFIX + reviewId).toString().toIntOrNull() ?: 0
        return dbViewCount + cachedCount
    }

    /**
     * 주기적으로 Redis의 조회수를 DB에 반영
     * 5분마다 실행
     */
    @Transactional
    @Scheduled(fixedRate = 300000) // 5분마다 실행
    fun syncViewCountsToDatabase() {
        val pattern = "review:viewCount:*"
        val keys = redisTemplate.keys(pattern)

        keys.forEach { key ->
            val reviewId = key.split(":").last().toLongOrNull()
            val count = redisTemplate.opsForValue().get(key)?.toString()?.toIntOrNull()

            if (reviewId != null && count != null && count > 0) {
                // 트랜잭션으로 DB 조회수 업데이트
                updateViewCountInDb(reviewId, count)
                // Redis 카운터 초기화
                redisTemplate.delete(key)
            }
        }
    }

    /**
     * DB 조회수 업데이트 메서드
     */
    private fun updateViewCountInDb(reviewId: Long, count: Int) {
        // JPA의 벌크 업데이트 쿼리 사용
        reviewRepository.bulkIncrementViewCount(reviewId, count)
    }

    fun getCurrentViewCounts(reviewIds: List<Long>): Map<Long, Int> {
        return reviewIds.associateWith { id ->
            getCurrentViewCount(id, 0)
        }
    }
}