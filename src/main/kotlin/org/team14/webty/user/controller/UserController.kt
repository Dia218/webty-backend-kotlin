package org.team14.webty.user.controller

import jakarta.validation.Valid
import lombok.extern.slf4j.Slf4j
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.team14.webty.security.authentication.WebtyUserDetails
import org.team14.webty.user.dto.ImageResponse
import org.team14.webty.user.dto.NicknameRequest
import org.team14.webty.user.dto.NicknameResponse
import org.team14.webty.user.dto.UserDataResponse
import org.team14.webty.user.mapper.UserDataResponseMapper
import org.team14.webty.user.service.UserService
import java.io.IOException

@RestController
@RequestMapping("/user")
@Slf4j
class UserController(
        private val userService: UserService
) {
    @PatchMapping("/nickname")
    fun changeNickname(
            @AuthenticationPrincipal webtyUserDetails: WebtyUserDetails,
            @RequestBody request: @Valid NicknameRequest
    ): ResponseEntity<NicknameResponse> {
        userService.modifyNickname(webtyUserDetails, request.nickname)
        return ResponseEntity.ok(NicknameResponse("닉네임이 변경되었습니다."))
    }

    @GetMapping("/info")
    fun getUserData(@AuthenticationPrincipal webtyUserDetails: WebtyUserDetails): ResponseEntity<UserDataResponse> {
        return ResponseEntity.ok(UserDataResponseMapper.toDto(userService.getAuthenticatedUser(webtyUserDetails)))
    }

    @PatchMapping("/profileImage")
    @Throws(IOException::class)
    fun changeProfileImg(
            @AuthenticationPrincipal webtyUserDetails: WebtyUserDetails,
            @RequestParam("file") file: MultipartFile
    ): ResponseEntity<ImageResponse> {
        userService.modifyImage(webtyUserDetails, file)
        return ResponseEntity.ok(ImageResponse("프로필사진이 변경되었습니다."))
    }

    @DeleteMapping("/users")
    fun delete(@AuthenticationPrincipal webtyUserDetails: WebtyUserDetails): ResponseEntity<Void> {
        userService.delete(webtyUserDetails)
        return ResponseEntity.noContent().build()
    }
}
