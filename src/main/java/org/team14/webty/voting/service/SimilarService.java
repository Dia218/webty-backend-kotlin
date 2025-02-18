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
import org.team14.webty.webtoon.service.WebtoonService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SimilarService {
	private final SimilarRepository similarRepository;
	private final WebtoonService webtoonService;
	private final AuthWebtyUserProvider authWebtyUserProvider;
	private final VoteRepository voteRepository;

	// 유사 웹툰 등록
	@Transactional
	public SimilarResponse createSimilar(WebtyUserDetails webtyUserDetails, Long targetWebtoonId,
		Long choiceWebtoonId) {
		WebtyUser webtyUser = authWebtyUserProvider.getAuthenticatedWebtyUser(webtyUserDetails);
		Webtoon targetWebtoon = webtoonService.findWebtoon(targetWebtoonId);
		Webtoon choiceWebtoon = webtoonService.findWebtoon(choiceWebtoonId);

		// 이미 등록된 유사 웹툰인지 확인
		if (similarRepository.existsByTargetWebtoonAndSimilarWebtoonId(targetWebtoon, choiceWebtoon.getWebtoonId())) {
			throw new BusinessException(ErrorCode.SIMILAR_DUPLICATION_ERROR);
		}

		Similar similar = SimilarMapper.toEntity(webtyUser.getUserId(), choiceWebtoon.getWebtoonId(), targetWebtoon);
		try {
			similarRepository.save(similar);
		} catch (DataIntegrityViolationException e) {
			// 데이터베이스에서 UNIQUE 제약 조건 위반 발생 시 처리
			throw new BusinessException(ErrorCode.SIMILAR_DUPLICATION_ERROR);
		}
		return SimilarMapper.toResponse(similar, choiceWebtoon);
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
	public Page<SimilarResponse> findAll(Long targetWebtoonId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Webtoon targetWebtoon = webtoonService.findWebtoon(targetWebtoonId);
		Page<Similar> similars = similarRepository.findAllByTargetWebtoon(targetWebtoon, pageable);

		return similars.map(
			similar -> SimilarMapper.toResponse(similar, webtoonService.findWebtoon(similar.getSimilarWebtoonId())));
	}
}
