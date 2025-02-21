package org.team14.webty.common.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ErrorDetails> {
        return ResponseEntity.status(e.errorCode.httpStatus).body(ErrorDetails.of(e))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception?): ResponseEntity<ErrorDetails> {
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.httpStatus)
            .body(
                ErrorDetails(
                    ErrorCode.INTERNAL_SERVER_ERROR.httpStatus,
                    ErrorCode.INTERNAL_SERVER_ERROR.errorCode,
                    ErrorCode.INTERNAL_SERVER_ERROR.message
                )
            )
    }
}