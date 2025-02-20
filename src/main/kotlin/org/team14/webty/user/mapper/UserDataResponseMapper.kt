package org.team14.webty.user.mapper

import org.team14.webty.user.dto.UserDataResponse
import org.team14.webty.user.entity.WebtyUser

object UserDataResponseMapper {
    fun toDto(user: WebtyUser): UserDataResponse {
        return UserDataResponse(
                userid = user.userId!!,
                nickname = user.nickname,
                profileImage = user.profileImage
        )
    }
}