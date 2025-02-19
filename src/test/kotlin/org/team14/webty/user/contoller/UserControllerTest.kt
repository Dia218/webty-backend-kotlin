package org.team14.webty.user.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.team14.webty.security.token.JwtManager
import org.team14.webty.user.entity.WebtyUser
import org.team14.webty.user.repository.UserRepository


@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = ["spring.profiles.active=test"])
class UserControllerTest {

    private val objectMapper = ObjectMapper()

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var jwtManager: JwtManager

    private lateinit var testUser: WebtyUser

    @BeforeEach
    fun beforeEach() {
        userRepository.deleteAll()
        testUser = userRepository.save(
            WebtyUser.builder()
                .nickname("테스트유저")
                .profileImage("testUserProfileImg")
                .build()
        )
    }

    @Test
    @DisplayName("닉네임 변경 테스트")
    fun changeNicknameTest() {
        val reqBody = mutableMapOf("nickname" to "새닉네임")
        val jsonRequest = objectMapper.writeValueAsString(reqBody)

        mockMvc.perform(
            patch("/user/nickname")
                .header("Authorization", "Bearer ${jwtManager.createAccessToken(testUser.userId)}")
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message", `is`("닉네임이 변경되었습니다.")))
    }

    @Test
    @DisplayName("사용자 정보 조회 테스트")
    fun getUserDataTest() {
        mockMvc.perform(
            get("/user/info")
                .header("Authorization", "Bearer ${jwtManager.createAccessToken(testUser.userId)}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("프로필 이미지 변경 테스트")
    fun changeProfileImageTest() {
        val file = MockMultipartFile(
            "file",
            "profile.jpg",
            "image/jpeg",
            "dummy image content".toByteArray()
        )

        mockMvc.perform(multipart("/user/profileImage")
            .file(file)
            .with { request -> request.method = "PATCH"; request }
            .header("Authorization", "Bearer ${jwtManager.createAccessToken(testUser.userId)}")
            .with(csrf())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message", `is`("프로필사진이 변경되었습니다.")))
    }

    @Test
    @DisplayName("사용자 삭제 테스트")
    fun deleteUserTest() {
        mockMvc.perform(
            delete("/user/users")
                .header("Authorization", "Bearer ${jwtManager.createAccessToken(testUser.userId)}")
                .with(csrf())
        )
            .andExpect(status().isNoContent)

        assertFalse { userRepository.findById(testUser.userId).isPresent }
    }
}
