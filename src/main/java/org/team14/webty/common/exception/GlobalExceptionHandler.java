package org.team14.webty.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorDetails> handleBusinessException(BusinessException e) {
		log.error("handleBusinessException", e);
		return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(ErrorDetails.of(e));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorDetails> handleException(Exception e) {
		log.error("handleException", e);
		return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
			.body(new ErrorDetails(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus(),
				ErrorCode.INTERNAL_SERVER_ERROR.getErrorCode(),
				ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
	}
}