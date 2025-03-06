package org.team14.webty.voting.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Pageable
import org.springframework.data.redis.RedisConnectionFailureException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.team14.webty.common.exception.BusinessException
import org.team14.webty.common.exception.ErrorCode
import org.team14.webty.common.mapper.PageMapper
import org.team14.webty.user.entity.WebtyUser
import org.team14.webty.voting.cache.VoteCacheService
import org.team14.webty.voting.dto.VoteFailureEvent
import org.team14.webty.voting.dto.VoteSuccessEvent
import org.team14.webty.voting.entity.Similar
import org.team14.webty.voting.entity.Vote
import org.team14.webty.voting.enums.VoteFailureType
import org.team14.webty.voting.enums.VoteType
import org.team14.webty.voting.mapper.SimilarMapper
import org.team14.webty.voting.mapper.VoteMapper.toEntity
import org.team14.webty.voting.redis.RedisPublisher
import org.team14.webty.voting.repository.SimilarRepository
import org.team14.webty.voting.repository.VoteRepository
import org.team14.webty.webtoon.repository.WebtoonRepository

@Service
class VoteService(
    private val voteRepository: VoteRepository,
    private val similarRepository: SimilarRepository,
    private val webtoonRepository: WebtoonRepository,
    private val redisPublisher: RedisPublisher,
    private val voteCacheService: VoteCacheService,
    private val eventPublisher: ApplicationEventPublisher
) {

    private val logger = KotlinLogging.logger {}

    // 유사 투표
    @Transactional
    fun vote(
        webtyUser: WebtyUser,
        similarId: Long,
        voteType: String,
        page: Int,
        size: Int
    ) {
        val similar =
            similarRepository.findByIdOrNull(similarId) ?: throw BusinessException(ErrorCode.SIMILAR_NOT_FOUND)

        if (voteCacheService.hasUserVoted(similarId, webtyUser.userId!!)) {
            throw BusinessException(ErrorCode.VOTE_ALREADY_EXISTS)
        }
        runCatching {
            val vote = toEntity(webtyUser, similar, voteType)
            voteRepository.save(vote)

            voteCacheService.setUserVote(similarId, webtyUser.userId!!)
            voteCacheService.incrementVote(similarId, vote.voteType)
            updateSimilarResult(similar)

            eventPublisher.publishEvent(VoteSuccessEvent(similarId, webtyUser.userId!!))
        }.onFailure { e ->
            handleFailure(e)
            eventPublisher.publishEvent(VoteFailureEvent(similarId, webtyUser.userId!!, VoteFailureType.VOTE_FAILURE))
            throw e
        }
    }

    // 투표 취소
    @Transactional
    fun cancel(webtyUser: WebtyUser, similarId: Long, page: Int, size: Int) {
        val similar =
            similarRepository.findById(similarId).orElseThrow { BusinessException(ErrorCode.SIMILAR_NOT_FOUND) }
        val vote = voteRepository.findBySimilarAndUserId(similar, webtyUser.userId!!)
            .orElseThrow { BusinessException(ErrorCode.VOTE_NOT_FOUND) }
        runCatching {
            voteRepository.delete(vote)

            voteCacheService.deleteUserVote(similarId, webtyUser.userId!!) // 캐시 삭제
            voteCacheService.decrementVote(similarId, vote.voteType)
            updateSimilarResult(similar)

            eventPublisher.publishEvent(VoteSuccessEvent(similarId, webtyUser.userId!!))
        }.onFailure { e ->
            handleFailure(e)
            eventPublisher.publishEvent(
                VoteFailureEvent(
                    similarId,
                    webtyUser.userId!!,
                    VoteFailureType.VOTE_CANCEL_FAILURE
                )
            )
            throw e
        }
    }

    // 투표 상태
    @Transactional
    fun getVoteStatus(webtyUser: WebtyUser, similarId: Long): Vote? {
        return voteRepository.findByUserIdAndSimilar_SimilarId(webtyUser.userId!!, similarId).orElse(null)
    }

    private fun updateSimilarResult(existingSimilar: Similar) { // DB조회 -> Redis 조회로 변경
        // agree 및 disagree 투표 개수 가져오기
        val agreeCount = voteCacheService.getVoteCount(existingSimilar.similarId!!, VoteType.AGREE) // 동의 수
        val disagreeCount = voteCacheService.getVoteCount(existingSimilar.similarId!!, VoteType.DISAGREE)  // 비동의 수

        // similarResult 업데이트
        val updateSimilar = existingSimilar.copy(similarResult = agreeCount - disagreeCount)
        similarRepository.save(updateSimilar)
    }

    fun publish(
        pageable: Pageable
    ) {
        val similars = similarRepository.findAllOrderBySimilarResultAndSimilarId(pageable)
        val similarResponsePageDto = similars.map { mapSimilar: Similar ->
            val agreeCount = voteCacheService.getVoteCount(mapSimilar.similarId!!, VoteType.AGREE) // 동의 수
            val disagreeCount = voteCacheService.getVoteCount(mapSimilar.similarId!!, VoteType.DISAGREE)  // 비동의 수

            SimilarMapper.toResponse( // 투표결과 + 투표 수 포함
                mapSimilar,
                webtoonRepository.findById(mapSimilar.similarWebtoonId)
                    .orElseThrow { BusinessException(ErrorCode.WEBTOON_NOT_FOUND) },
                agreeCount,
                disagreeCount
            )
        }.let { PageMapper.toPageDto(it) }

        redisPublisher.publish("vote-results", similarResponsePageDto)
    }

    private fun handleFailure(e: Throwable) {
        when (e) {
            is BusinessException -> {
                logger.info { "BusinessException 발생" }
            }

            is RedisConnectionFailureException -> {
                logger.info { "Redis 연결 오류 발생" }
            }

            else -> {
                logger.info { "알 수 없는 오류 발생" }
            }
        }
    }
}
