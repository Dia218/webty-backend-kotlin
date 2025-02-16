package org.team14.webty.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {

	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-001", "유저를 찾을 수 없습니다."),
	USER_LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "USER-002", "로그인이 필요합니다."),
	USER_NICKNAME_DUPLICATION(HttpStatus.BAD_REQUEST, "USER-003", "닉네임이 중복 되었습니다."),
	TOKEN_NOT_VALID(HttpStatus.UNAUTHORIZED, "TOKEN-001", "토큰이 유효하지 않습니다."),
	WEBTOON_NOT_FOUND(HttpStatus.NOT_FOUND, "WEBTOON-001", "웹툰을 찾을 수 없습니다."),
	ALREADY_FAVORITED_WEBTOON(HttpStatus.BAD_REQUEST, "WEBTOON-002", "이미 관심 웹툰으로 등록되었습니다."),
	REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW-001", "리뷰를 찾을 수 없습니다."),
	REVIEW_PERMISSION_DENIED(HttpStatus.UNAUTHORIZED, "REVIEW-002", "리뷰에 대한 삭제/수정 권한이 없습니다."),
	RECOMMEND_DUPLICATION_ERROR(HttpStatus.BAD_REQUEST, "RECOMMEND-001", "추천/비추천을 두번 이상 할 수 없습니다."),
	RECOMMEND_TYPE_ERROR(HttpStatus.BAD_REQUEST, "RECOMMEND-002", "type은 LIKE(like), HATE(hate)만 가능합니다."),
	RECOMMEND_NOT_FOUND(HttpStatus.NOT_FOUND, "RECOMMEND-003", "해당 추천/비추천이 존재하지 않습니다."),
	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT-001", "댓글을 찾을 수 없습니다."),
	COMMENT_WRITING_RESTRICTED(HttpStatus.BAD_REQUEST, "COMMENT-002", "더 이상 대댓글을 작성할 수 없습니다."),
	COMMENT_PERMISSION_DENIED(HttpStatus.UNAUTHORIZED, "COMMENT-003", "댓글에 대한 삭제/수정 권한이 없습니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL-001", "서버 내부 오류"),
	FILE_UPLOAD_TYPE_ERROR(HttpStatus.BAD_REQUEST, "FILE-001", "이미지파일 이외의 업로드는 불가능합니다."),
	FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "FILE-002", "파일 업로드 중 오류가 발생했습니다."),
	FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE-003", "파일 삭제 중 오류가 발생했습니다."),
	DIRECTORY_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE-004", "디렉토리 생성 중 오류가 발생했습니다."),
	SIMILAR_NOT_FOUND(HttpStatus.NOT_FOUND, "SIMILAR-001", "유사웹툰이 등록 되어 있지 않습니다."),
	SIMILAR_DUPLICATION_ERROR(HttpStatus.BAD_REQUEST, "SIMILAR-002", "이미 등록된 유사웹툰입니다."),
	INVALID_SIMILAR_NAME(HttpStatus.BAD_REQUEST, "SIMILAR-003", "유사웹툰 이름이 유효하지 않습니다."),
	VOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "VOTE-001", "투표 내역을 찾을 수 없습니다."),
	VOTE_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "VOTE-002", "이미 투표한 내역이 존재합니다.");

	private final HttpStatus httpStatus;
	private final String errorCode;
	private final String message;

	ErrorCode(HttpStatus httpStatus, String errorCode, String message) {
		this.httpStatus = httpStatus;
		this.errorCode = errorCode;
		this.message = message;
	}
}
