package org.team14.webty.voting.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.team14.webty.common.exception.BusinessException;
import org.team14.webty.common.exception.ErrorCode;
import org.team14.webty.security.authentication.AuthWebtyUserProvider;
import org.team14.webty.security.authentication.WebtyUserDetails;
import org.team14.webty.user.entity.WebtyUser;
import org.team14.webty.voting.dto.SimilarResponse;
import org.team14.webty.voting.entity.Similar;
import org.team14.webty.voting.mapper.SimilarMapper;
import org.team14.webty.voting.repository.SimilarRepository;
import org.team14.webty.voting.repository.VoteRepository;
import org.team14.webty.webtoon.entity.Webtoon;
import org.team14.webty.webtoon.repository.WebtoonRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SimilarService {
	private final SimilarRepository similarRepository;
	private final WebtoonRepository webtoonRepository;
	private final AuthWebtyUserProvider authWebtyUserProvider;
	private final VoteRepository voteRepository;

	// 유사 웹툰 등록
	@Transactional
	public Long createSimilar(WebtyUserDetails webtyUserDetails, Long webtoonId, String similarWebtoonName) {
		WebtyUser webtyUser = authWebtyUserProvider.getAuthenticatedWebtyUser(webtyUserDetails);
		Webtoon webtoon = webtoonRepository.findById(webtoonId)
			.orElseThrow(() -> new BusinessException(ErrorCode.WEBTOON_NOT_FOUND));
		if (similarWebtoonName == null || similarWebtoonName.trim().isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_SIMILAR_NAME);
		}
		// 이미 등록된 유사 웹툰인지 확인
		if (similarRepository.existsByWebtoonAndSimilarWebtoonName(webtoon, similarWebtoonName)) {
			throw new BusinessException(ErrorCode.SIMILAR_DUPLICATION_ERROR);
		}
		Similar similar = SimilarMapper.toEntity(webtyUser.getUserId(), similarWebtoonName, webtoon);
		try {
			similarRepository.save(similar);
		} catch (DataIntegrityViolationException e) {
			// 데이터베이스에서 UNIQUE 제약 조건 위반 발생 시 처리
			throw new BusinessException(ErrorCode.SIMILAR_DUPLICATION_ERROR);
		}
		return similar.getSimilarId();
	}

	// 유사 웹툰 삭제
	@Transactional
	public void deleteSimilar(WebtyUserDetails webtyUserDetails, Long similarId) {
		WebtyUser webtyUser = authWebtyUserProvider.getAuthenticatedWebtyUser(webtyUserDetails);
		Similar similar = similarRepository.findByUserIdAndSimilarId(webtyUser.getUserId(),
				similarId)
			.orElseThrow(() -> new BusinessException(ErrorCode.SIMILAR_NOT_FOUND));
		voteRepository.deleteAll(voteRepository.findAllBySimilar(similar)); // 연관된 투표내역도 삭제
		similarRepository.delete(similar);
	}

	// 유사 리스트 By Webtoon
	@Transactional(readOnly = true)
	public Page<SimilarResponse> findAll(Long webtoonId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Webtoon webtoon = webtoonRepository.findById(webtoonId)
			.orElseThrow(() -> new BusinessException(ErrorCode.WEBTOON_NOT_FOUND));
		Page<Similar> similars = similarRepository.findAllByWebtoon(webtoon, pageable);
		return similars.map(SimilarMapper::toResponse);
	}

	@Transactional(readOnly = true)
	public SimilarResponse getSimilar(Long similarId) {
		Similar similar = similarRepository.findById(similarId)
			.orElseThrow(() -> new BusinessException(ErrorCode.SIMILAR_NOT_FOUND));
		return SimilarMapper.toResponse(similar);
	}
}
