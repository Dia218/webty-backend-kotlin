package org.team14.webty.voting.cache

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.team14.webty.voting.enums.VoteType

@Service
class VoteCacheService(private val redisTemplate: RedisTemplate<String, String>) {

    private fun getKey(similarId: Long, voteType: VoteType): String { // redis 조회용 키 반환
        return "vote:$similarId:$voteType"
    }

    fun incrementVote(similarId: Long, voteType: VoteType) { // 선택한 similar.voteType 개수 1 증가
        val key = getKey(similarId, voteType)
        redisTemplate.opsForValue().increment(key)
    }

    fun decrementVote(similarId: Long, voteType: VoteType) { // 선택한 similar.voteType 개수 1 감소
        val key = getKey(similarId, voteType)
        redisTemplate.opsForValue().decrement(key)
    }

    fun getVoteCount(similarId: Long, voteType: VoteType): Long { // 선택한 similar.voteType 의 개수 조회
        val key = getKey(similarId, voteType)
        return redisTemplate.opsForValue().get(key)?.toLong() ?: 0
    }

    fun setVoteCount(similarId: Long, voteType: VoteType, count: Long) { // 선택한 similar.voteType 의 개수 설정
        val key = getKey(similarId, voteType)
        redisTemplate.opsForValue().set(key, count.toString())
    }

    fun deleteVotesForSimilar(similarId: Long) { // 선택한 similar 관련 투표 전체 삭제
        VoteType.entries.forEach { voteType ->
            val key = getKey(similarId, voteType)
            redisTemplate.delete(key)
        }
    }
}
