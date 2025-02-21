package org.team14.webty.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.team14.webty.user.entity.SocialProvider
import org.team14.webty.user.entity.WebtyUser
import java.util.*

@Repository
interface UserRepository : JpaRepository<WebtyUser, Long> {
    fun findByNickname(nickname: String): Optional<WebtyUser>
    fun findBySocialProvider(socialProvider: SocialProvider): Optional<WebtyUser>
    fun existsByNickname(nickname: String): Boolean
}
