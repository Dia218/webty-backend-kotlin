package org.team14.webty.review.cache

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.team14.webty.review.repository.ReviewRepository
import org.slf4j.LoggerFactory

@Service
class ViewCountCacheService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val reviewRepository: ReviewRepository
) {
    private val log = LoggerFactory.getLogger(ViewCountCacheService::class.java)
    
    // Redis 키 패턴
    companion object {
        private const val VIEW_COUNT_KEY_PREFIX = "review:viewCount:"
        private const val SYNC_DELAY = 3000L // 3초마다 동기화
    }

    /**
     * 조회수 증가 메서드
     * 조회가 발생할 때마다 Redis의 카운터 증가
     */
    @Transactional
    fun incrementViewCount(reviewId: Long) {
        redisTemplate.opsForValue().increment(VIEW_COUNT_KEY_PREFIX + reviewId)
        // 조회수 증가 로그 (개발 환경에서만)
        log.debug("조회수 증가: 리뷰 ID = $reviewId")
    }

    /**
     * 현재 조회수 조회 메서드
     * 캐시된 조회수 + DB 조회수 합산 반환
     */
    fun getCurrentViewCount(reviewId: Long, dbViewCount: Int): Int {
        val cachedCount = getCachedCount(reviewId)
        return dbViewCount + cachedCount
    }
    
    /**
     * Redis에서 캐시된 조회수만 가져오는 헬퍼 메서드
     */
    private fun getCachedCount(reviewId: Long): Int {
        return try {
            val value = redisTemplate.opsForValue().get(VIEW_COUNT_KEY_PREFIX + reviewId)
            value?.toString()?.toIntOrNull() ?: 0
        } catch (e: Exception) {
            log.error("Redis에서 조회수 조회 중 오류: $e")
            0
        }
    }

    /**
     * 주기적으로 Redis의 조회수를 DB에 반영
     * 3초마다 실행
     */
    @Transactional
    @Scheduled(fixedRate = SYNC_DELAY)
    fun syncViewCountsToDatabase() {
        try {
            val pattern = "review:viewCount:*"
            val keys = redisTemplate.keys(pattern)
            
            if (keys.isEmpty()) {
                return
            }
            
            log.debug("조회수 동기화 시작: ${keys.size}개 항목")
            
            keys.forEach { key ->
                val reviewId = key.split(":").last().toLongOrNull()
                val count = redisTemplate.opsForValue().get(key)?.toString()?.toIntOrNull()

                if (reviewId != null && count != null && count > 0) {
                    try {
                        // 트랜잭션으로 DB 조회수 업데이트
                        updateViewCountInDb(reviewId, count)
                        // Redis 카운터 초기화
                        redisTemplate.delete(key)
                        log.debug("조회수 동기화 완료: 리뷰 ID = $reviewId, 증가량 = $count")
                    } catch (e: Exception) {
                        log.error("조회수 업데이트 중 오류 (리뷰 ID: $reviewId): $e")
                    }
                }
            }
        } catch (e: Exception) {
            log.error("조회수 동기화 중 오류 발생: $e")
        }
    }

    /**
     * DB 조회수 업데이트 메서드
     */
    private fun updateViewCountInDb(reviewId: Long, count: Int) {
        // JPA의 벌크 업데이트 쿼리 사용
        reviewRepository.bulkIncrementViewCount(reviewId, count)
    }

    /**
     * 여러 리뷰의 현재 조회수 한번에 조회
     */
    fun getCurrentViewCounts(reviewIds: List<Long>): Map<Long, Int> {
        if (reviewIds.isEmpty()) {
            return emptyMap()
        }
        
        // 리뷰 ID별 DB 조회수 맵 구성
        val dbViewCounts = reviewRepository.findAllById(reviewIds)
            .associateBy({ it.reviewId!! }, { it.viewCount })
        
        // 각 리뷰 ID에 대해 캐시된 조회수와 DB 조회수 합산
        return reviewIds.associateWith { id ->
            val dbCount = dbViewCounts[id] ?: 0
            val cachedCount = getCachedCount(id)
            dbCount + cachedCount
        }
    }
}