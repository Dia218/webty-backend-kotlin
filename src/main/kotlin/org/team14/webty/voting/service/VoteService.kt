package org.team14.webty.voting.service

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.team14.webty.common.exception.BusinessException
import org.team14.webty.common.exception.ErrorCode
import org.team14.webty.common.mapper.PageMapper
import org.team14.webty.common.redis.RedisPublisher
import org.team14.webty.security.authentication.AuthWebtyUserProvider
import org.team14.webty.security.authentication.WebtyUserDetails
import org.team14.webty.voting.entity.Similar
import org.team14.webty.voting.enums.VoteType
import org.team14.webty.voting.mapper.SimilarMapper
import org.team14.webty.voting.mapper.VoteMapper.toEntity
import org.team14.webty.voting.repository.SimilarRepository
import org.team14.webty.voting.repository.VoteRepository
import org.team14.webty.webtoon.repository.WebtoonRepository

@Service
class VoteService(
    private val voteRepository: VoteRepository,
    private val similarRepository: SimilarRepository,
    private val authWebtyUserProvider: AuthWebtyUserProvider,
    private val webtoonRepository: WebtoonRepository,
    private val redisPublisher: RedisPublisher
) {
    // 유사 투표
    @Transactional
    fun vote(
        webtyUserDetails: WebtyUserDetails,
        similarId: Long,
        voteType: String,
        page: Int,
        size: Int
    ) {
        val pageable: Pageable = PageRequest.of(page, size)
        val webtyUser = authWebtyUserProvider.getAuthenticatedWebtyUser(webtyUserDetails)
        val similar = similarRepository.findById(similarId)
            .orElseThrow { BusinessException(ErrorCode.SIMILAR_NOT_FOUND) }!!
        // 중복 투표 방지
        if (voteRepository.existsByUserIdAndSimilar(webtyUser.userId!!, similar)) {
            throw BusinessException(ErrorCode.VOTE_ALREADY_EXISTS)
        }
        val vote = toEntity(webtyUser, similar, voteType)
        voteRepository.save(vote)
        updateSimilarResult(similar)
        publish(similar, pageable)
    }

    // 투표 취소
    @Transactional
    fun cancel(webtyUserDetails: WebtyUserDetails, similarId: Long, page: Int, size: Int) {
        val pageable: Pageable = PageRequest.of(page, size)
        val webtyUser = authWebtyUserProvider.getAuthenticatedWebtyUser(webtyUserDetails)
        val similar =
            similarRepository.findById(similarId).orElseThrow { BusinessException(ErrorCode.SIMILAR_NOT_FOUND) }
        val vote = voteRepository.findBySimilarAndUserId(similar, webtyUser.userId!!)
            .orElseThrow { BusinessException(ErrorCode.VOTE_NOT_FOUND) }
        voteRepository.delete(vote)
        updateSimilarResult(vote.similar)
        publish(similar, pageable)
    }

    private fun updateSimilarResult(existingSimilar: Similar) {
        // agree 및 disagree 투표 개수 가져오기
        val agreeCount = voteRepository.countBySimilarAndVoteType(existingSimilar, VoteType.AGREE) // 동의 수
        val disagreeCount = voteRepository.countBySimilarAndVoteType(existingSimilar, VoteType.DISAGREE) // 비동의 수

        // similarResult 업데이트
        val updateSimilar = existingSimilar.copy(similarResult = agreeCount - disagreeCount)
        similarRepository.save<Similar>(updateSimilar)
    }

    private fun publish(
        similar: Similar,
        pageable: Pageable
    ) {
        val similars = similarRepository.findAllByTargetWebtoon(similar.targetWebtoon, pageable)
        val similarResponsePageDto = similars.map { mapSimilar: Similar ->
            SimilarMapper.toResponse(
                mapSimilar,
                webtoonRepository.findById(mapSimilar.similarWebtoonId)
                    .orElseThrow { BusinessException(ErrorCode.WEBTOON_NOT_FOUND) }
            )
        }.let { PageMapper.toPageDto(it) }

        redisPublisher.publish("vote-results", similarResponsePageDto)
    }
}
