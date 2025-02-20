package org.team14.webty.webtoon.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.team14.webty.security.authentication.WebtyUserDetails
import org.team14.webty.webtoon.dto.WebtoonDetailDto
import org.team14.webty.webtoon.service.FavoriteService


@RestController
@RequestMapping("/favorite")
class FavoriteController(
    private val favoriteService: FavoriteService
) {

    @PostMapping("/{webtoonId}")
    fun add(
        @AuthenticationPrincipal webtyUserDetails: WebtyUserDetails,
        @PathVariable webtoonId: Long
    ): ResponseEntity<Void> {
        favoriteService.addFavorite(webtyUserDetails, webtoonId)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{webtoonId}")
    fun delete(
        @AuthenticationPrincipal webtyUserDetails: WebtyUserDetails,
        @PathVariable webtoonId: Long
    ): ResponseEntity<Void> {
        favoriteService.deleteFavorite(webtyUserDetails, webtoonId)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/list")
    fun getUserFavorite(
        @AuthenticationPrincipal webtyUserDetails: WebtyUserDetails
    ): ResponseEntity<List<WebtoonDetailDto>> {
        val userFavorites = favoriteService.getUserFavorites(webtyUserDetails)
        return ResponseEntity.ok(userFavorites)
    }

    @GetMapping("/{webtoonId}")
    fun checkFavorite(
        @AuthenticationPrincipal webtyUserDetails: WebtyUserDetails,
        @PathVariable webtoonId: Long
    ): ResponseEntity<Boolean> {
        val isFavorite = favoriteService.checkFavoriteWebtoon(webtyUserDetails, webtoonId)
        return ResponseEntity.ok(isFavorite)
    }
}