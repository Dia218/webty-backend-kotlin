package org.team14.webty.user.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.team14.webty.security.authentication.WebtyUserDetails;
import org.team14.webty.user.dto.ImageResponse;
import org.team14.webty.user.dto.NicknameRequest;
import org.team14.webty.user.dto.NicknameResponse;
import org.team14.webty.user.dto.UserDataResponse;
import org.team14.webty.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController { // 예외 처리 추가 필요

	private final UserService userService;

	@PatchMapping("/nickname")
	public ResponseEntity<NicknameResponse> changeNickname(
		@AuthenticationPrincipal WebtyUserDetails webtyUserDetails,
		@RequestBody @Valid NicknameRequest request) {
		// if (webtyUserDetails == null) {
		// 	return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
		// 		.body(new NicknameResponse("로그인이 필요합니다."));
		// } //dto 수정 작업과 함께 예외 처리 추가 필요
		userService.modifyNickname(webtyUserDetails, request.getNickname());
		return ResponseEntity.ok(new NicknameResponse("닉네임이 변경되었습니다."));
	}
	
	@GetMapping("/info")
	public ResponseEntity<UserDataResponse> getUserData(@AuthenticationPrincipal WebtyUserDetails webtyUserDetails) {
		return ResponseEntity.ok(new UserDataResponse(userService.getAuthenticatedUser(webtyUserDetails)));
	}

	@PatchMapping("/profileImage")
	public ResponseEntity<ImageResponse> changeProfileImg(
		@AuthenticationPrincipal WebtyUserDetails webtyUserDetails,
		@RequestParam("file") MultipartFile file) throws IOException {
		userService.modifyImage(webtyUserDetails, file);
		return ResponseEntity.ok(new ImageResponse("프로필사진이 변경되었습니다."));
	}

	@DeleteMapping("/users")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal WebtyUserDetails webtyUserDetails) {
		userService.delete(webtyUserDetails);
		return ResponseEntity.noContent().build();
	}

}

