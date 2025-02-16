package org.team14.webty.webtoon.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.team14.webty.common.exception.BusinessException;
import org.team14.webty.common.exception.ErrorCode;
import org.team14.webty.security.authentication.AuthWebtyUserProvider;
import org.team14.webty.security.authentication.WebtyUserDetails;
import org.team14.webty.user.entity.WebtyUser;
import org.team14.webty.webtoon.dto.WebtoonDetailDto;
import org.team14.webty.webtoon.entity.Favorite;
import org.team14.webty.webtoon.entity.Webtoon;
import org.team14.webty.webtoon.mapper.FavoriteMapper;
import org.team14.webty.webtoon.mapper.WebtoonApiResponseMapper;
import org.team14.webty.webtoon.repository.FavoriteRepository;
import org.team14.webty.webtoon.repository.WebtoonRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteService {

	private final FavoriteRepository favoriteRepository;
	private final WebtoonRepository webtoonRepository;
	private final AuthWebtyUserProvider authWebtyUserProvider;

	@Transactional
	public void addFavorite(WebtyUserDetails webtyUserDetails, Long webtoonId) {
		WebtyUser webtyUser = authWebtyUserProvider.getAuthenticatedWebtyUser(webtyUserDetails);

		Webtoon webtoon = webtoonRepository.findById(webtoonId)
			.orElseThrow(() -> new BusinessException(ErrorCode.WEBTOON_NOT_FOUND));

		if (favoriteRepository.findByWebtyUserAndWebtoon(webtyUser, webtoon).isPresent()) {
			throw new BusinessException(ErrorCode.ALREADY_FAVORITED_WEBTOON);
		}

		favoriteRepository.save(FavoriteMapper.toEntity(webtyUser, webtoon));
	}

	@Transactional
	public void deleteFavorite(WebtyUserDetails webtyUserDetails, Long webtoonId) {
		WebtyUser webtyUser = authWebtyUserProvider.getAuthenticatedWebtyUser(webtyUserDetails);

		Webtoon webtoon = webtoonRepository.findById(webtoonId)
			.orElseThrow(() -> new BusinessException(ErrorCode.WEBTOON_NOT_FOUND));

		favoriteRepository.deleteByWebtyUserAndWebtoon(webtyUser, webtoon);
	}

	@Transactional(readOnly = true)
	public List<WebtoonDetailDto> getUserFavorites(WebtyUserDetails webtyUserDetails) {
		WebtyUser webtyUser = authWebtyUserProvider.getAuthenticatedWebtyUser(webtyUserDetails);

		return favoriteRepository.findByWebtyUser(webtyUser)
			.stream()
			.map(Favorite::getWebtoon)
			.map(WebtoonApiResponseMapper::toDto)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public boolean checkFavoriteWebtoon(WebtyUserDetails webtyUserDetails, Long webtoonId) {
		WebtyUser webtyUser = authWebtyUserProvider.getAuthenticatedWebtyUser(webtyUserDetails);

		Webtoon webtoon = webtoonRepository.findById(webtoonId)
			.orElseThrow(() -> new BusinessException(ErrorCode.WEBTOON_NOT_FOUND));

		return favoriteRepository.findByWebtyUserAndWebtoon(webtyUser, webtoon).isPresent();
	}
}