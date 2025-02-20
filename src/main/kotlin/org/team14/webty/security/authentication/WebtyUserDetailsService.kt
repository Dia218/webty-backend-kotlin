package org.team14.webty.security.authentication

import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import org.team14.webty.common.exception.BusinessException
import org.team14.webty.common.exception.ErrorCode
import org.team14.webty.user.service.UserService

@Service
class WebtyUserDetailsService(
    private val userService: UserService
) : UserDetailsService {

    override fun loadUserByUsername(username: String): WebtyUserDetails =
        runCatching { WebtyUserDetails(userService.findByNickName(username)) }
            .getOrElse { throw BusinessException(ErrorCode.USER_NOT_FOUND) }

    fun loadUserByUserId(userId: Long): WebtyUserDetails {
        val username = userService.findNickNameByUserId(userId)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
        return loadUserByUsername(username)
    }
}
