package org.team14.webty.webtoon.service

import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import org.team14.webty.common.exception.BusinessException
import org.team14.webty.common.exception.ErrorCode
import org.team14.webty.security.authentication.AuthWebtyUserProvider
import org.team14.webty.security.authentication.WebtyUserDetails
import org.team14.webty.webtoon.dto.WebtoonDetailDto
import org.team14.webty.webtoon.mapper.FavoriteMapper
import org.team14.webty.webtoon.mapper.WebtoonApiResponseMapper
import org.team14.webty.webtoon.repository.FavoriteRepository
import org.team14.webty.webtoon.repository.WebtoonRepository


@Service
class FavoriteService(
    private val favoriteRepository: FavoriteRepository,
    private val webtoonRepository: WebtoonRepository,
    private val authWebtyUserProvider: AuthWebtyUserProvider
) {

    @Transactional
    fun addFavorite(webtyUserDetails: WebtyUserDetails, webtoonId: Long) {
        val webtyUser = authWebtyUserProvider.getAuthenticatedWebtyUser(webtyUserDetails)

        val webtoon = webtoonRepository.findById(webtoonId)
            .orElseThrow { BusinessException(ErrorCode.WEBTOON_NOT_FOUND) }

        if (favoriteRepository.findByWebtyUserAndWebtoon(webtyUser, webtoon).isPresent) {
            throw BusinessException(ErrorCode.ALREADY_FAVORITED_WEBTOON)
        }

        favoriteRepository.save(FavoriteMapper.toEntity(webtyUser, webtoon))
    }

    @Transactional
    fun deleteFavorite(webtyUserDetails: WebtyUserDetails, webtoonId: Long) {
        val webtyUser = authWebtyUserProvider.getAuthenticatedWebtyUser(webtyUserDetails)

        val webtoon = webtoonRepository.findById(webtoonId)
            .orElseThrow { BusinessException(ErrorCode.WEBTOON_NOT_FOUND) }

        favoriteRepository.deleteByWebtyUserAndWebtoon(webtyUser, webtoon)
    }

    @Transactional(readOnly = true)
    fun getUserFavorites(webtyUserDetails: WebtyUserDetails): List<WebtoonDetailDto> {
        val webtyUser = authWebtyUserProvider.getAuthenticatedWebtyUser(webtyUserDetails)

        return favoriteRepository.findByWebtyUser(webtyUser)
            .map { it.webtoon }
            .map(WebtoonApiResponseMapper::toDto)
    }

    @Transactional(readOnly = true)
    fun checkFavoriteWebtoon(webtyUserDetails: WebtyUserDetails, webtoonId: Long): Boolean {
        val webtyUser = authWebtyUserProvider.getAuthenticatedWebtyUser(webtyUserDetails)

        val webtoon = webtoonRepository.findById(webtoonId)
            .orElseThrow { BusinessException(ErrorCode.WEBTOON_NOT_FOUND) }

        return favoriteRepository.findByWebtyUserAndWebtoon(webtyUser, webtoon).isPresent
    }
}