package org.team14.webty.webtoon.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.team14.webty.user.entity.WebtyUser
import org.team14.webty.webtoon.entity.Favorite
import org.team14.webty.webtoon.entity.Webtoon
import java.util.*

@Repository
interface FavoriteRepository : JpaRepository<Favorite, Long> {

    fun findByWebtyUserAndWebtoon(webtyUser: WebtyUser, webtoon: Webtoon): Optional<Favorite>

    fun deleteByWebtyUserAndWebtoon(webtyUser: WebtyUser, webtoon: Webtoon)

    fun findByWebtyUser(user: WebtyUser): List<Favorite>
}