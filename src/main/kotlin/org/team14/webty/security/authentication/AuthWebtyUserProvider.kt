package org.team14.webty.security.authentication

import org.springframework.stereotype.Component
import org.team14.webty.common.exception.BusinessException
import org.team14.webty.common.exception.ErrorCode
import org.team14.webty.user.entity.WebtyUser

@Component
class AuthWebtyUserProvider {
    fun getAuthenticatedWebtyUser(webtyUserDetails: WebtyUserDetails?): WebtyUser {
        if (webtyUserDetails == null) {
            throw BusinessException(ErrorCode.USER_LOGIN_REQUIRED)
        }

        return webtyUserDetails.webtyUser
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
    }
}
