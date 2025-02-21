package org.team14.webty.common.exception

import org.springframework.http.HttpStatus

data class ErrorDetails(
    val httpStatus: HttpStatus,
    val errorCode: String,
    val message: String,
) {
    companion object {
        fun of(e: BusinessException): ErrorDetails {
            return ErrorDetails(
                httpStatus = e.errorCode.httpStatus,
                errorCode = e.errorCode.errorCode,
                message = e.message ?: "알 수 없는 에러"
            )
        }
    }
}