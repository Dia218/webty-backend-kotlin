package org.team14.webty.webtoon.mapper

import org.team14.webty.user.entity.WebtyUser
import org.team14.webty.webtoon.entity.Favorite
import org.team14.webty.webtoon.entity.Webtoon

object FavoriteMapper {
    fun toEntity(webtyUser: WebtyUser, webtoon: Webtoon): Favorite {
        return Favorite(
            webtyUser = webtyUser,
            webtoon = webtoon
        )
    }
}