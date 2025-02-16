package org.team14.webty.voting.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.team14.webty.common.exception.BusinessException;
import org.team14.webty.common.exception.ErrorCode;
import org.team14.webty.security.authentication.AuthWebtyUserProvider;
import org.team14.webty.security.authentication.WebtyUserDetails;
import org.team14.webty.user.entity.WebtyUser;
import org.team14.webty.voting.dto.VoteRequest;
import org.team14.webty.voting.entity.Similar;
import org.team14.webty.voting.entity.Vote;
import org.team14.webty.voting.enumerate.VoteType;
import org.team14.webty.voting.mapper.VoteMapper;
import org.team14.webty.voting.repository.SimilarRepository;
import org.team14.webty.voting.repository.VoteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoteService {
	private final VoteRepository voteRepository;
	private final SimilarRepository similarRepository;
	private final AuthWebtyUserProvider authWebtyUserProvider;

	// 유사 투표
	@Transactional
	public Long vote(WebtyUserDetails webtyUserDetails, VoteRequest voteRequest) {
		WebtyUser webtyUser = authWebtyUserProvider.getAuthenticatedWebtyUser(webtyUserDetails);
		Similar similar = similarRepository.findById(voteRequest.getSimilarId())
			.orElseThrow(() -> new BusinessException(ErrorCode.SIMILAR_NOT_FOUND));
		// 중복 투표 방지
		if (voteRepository.existsByUserIdAndSimilar(webtyUser.getUserId(), similar)) {
			throw new BusinessException(ErrorCode.VOTE_ALREADY_EXISTS);
		}
		Vote vote = VoteMapper.toEntity(webtyUser, similar, voteRequest.getVoteType());
		voteRepository.save(vote);
		updateSimilarResult(similar);
		return vote.getVoteId();
	}

	// 투표 취소
	@Transactional
	public void cancel(WebtyUserDetails webtyUserDetails, Long voteId) {
		authWebtyUserProvider.getAuthenticatedWebtyUser(webtyUserDetails);
		Vote vote = voteRepository.findById(voteId)
			.orElseThrow(() -> new BusinessException(ErrorCode.VOTE_NOT_FOUND));
		voteRepository.delete(vote);
		updateSimilarResult(vote.getSimilar());
	}

	private void updateSimilarResult(Similar similar) {
		// agree 및 disagree 투표 개수 가져오기
		long agreeCount = voteRepository.countBySimilarAndVoteType(similar, VoteType.AGREE); // 동의 수
		long disagreeCount = voteRepository.countBySimilarAndVoteType(similar, VoteType.DISAGREE); // 비동의 수

		// similarResult 업데이트
		similar.updateSimilarResult(agreeCount - disagreeCount);
		similarRepository.save(similar);
	}

}
