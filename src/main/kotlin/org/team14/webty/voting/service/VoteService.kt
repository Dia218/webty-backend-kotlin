package org.team14.webty.voting.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.team14.webty.common.exception.BusinessException
import org.team14.webty.common.exception.ErrorCode
import org.team14.webty.security.authentication.AuthWebtyUserProvider
import org.team14.webty.security.authentication.WebtyUserDetails
import org.team14.webty.voting.dto.VoteRequest
import org.team14.webty.voting.entity.Similar
import org.team14.webty.voting.entity.Vote
import org.team14.webty.voting.enums.VoteType
import org.team14.webty.voting.mapper.VoteMapper.toEntity
import org.team14.webty.voting.repository.SimilarRepository
import org.team14.webty.voting.repository.VoteRepository
import java.util.function.Supplier

@Service
class VoteService(
    private val voteRepository: VoteRepository,
    private val similarRepository: SimilarRepository,
    private val authWebtyUserProvider: AuthWebtyUserProvider
) {
    // 유사 투표
    @Transactional
    fun vote(webtyUserDetails: WebtyUserDetails, voteRequest: VoteRequest): Long {
        val webtyUser = authWebtyUserProvider.getAuthenticatedWebtyUser(webtyUserDetails)
        val similar = similarRepository.findById(voteRequest.similarId)
            .orElseThrow<BusinessException> { BusinessException(ErrorCode.SIMILAR_NOT_FOUND) }!!
        // 중복 투표 방지
        if (voteRepository.existsByUserIdAndSimilar(webtyUser.userId!!, similar)) {
            throw BusinessException(ErrorCode.VOTE_ALREADY_EXISTS)
        }
        val vote = toEntity(webtyUser, similar, voteRequest.voteType)
        voteRepository.save(vote)
        updateSimilarResult(similar)
        return vote.voteId!! // to do: 투표 결과 계산 및 이에 따라 정렬하여 PageDto<SimilarResponse> 를 반환하도록 수정
    }

    // 투표 취소
    @Transactional
    fun cancel(webtyUserDetails: WebtyUserDetails, voteId: Long) {
        authWebtyUserProvider.getAuthenticatedWebtyUser(webtyUserDetails)
        val vote: Vote = voteRepository.findById(voteId)
            .orElseThrow(Supplier { BusinessException(ErrorCode.VOTE_NOT_FOUND) })
        voteRepository.delete(vote)
        updateSimilarResult(vote.similar)
    }

    private fun updateSimilarResult(existingSimilar: Similar) {
        // agree 및 disagree 투표 개수 가져오기
        val agreeCount = voteRepository.countBySimilarAndVoteType(existingSimilar, VoteType.AGREE) // 동의 수
        val disagreeCount = voteRepository.countBySimilarAndVoteType(existingSimilar, VoteType.DISAGREE) // 비동의 수

        // similarResult 업데이트
        val updateSimilar = existingSimilar.copy(similarResult = agreeCount - disagreeCount)
        similarRepository.save<Similar>(updateSimilar)
    }
}
