package org.team14.webty.user.dto

data class UserDataResponse(
        // UserInfoResponse가 스프링에 이미 있어서 UserDataResponse 사용
        val userid: Long,
        val nickname: String,
        val profileImage: String
)
