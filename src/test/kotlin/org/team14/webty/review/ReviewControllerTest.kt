package org.team14.webty.review.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.team14.webty.review.dto.ReviewRequest
import org.team14.webty.review.entity.Review
import org.team14.webty.review.enumrate.SpoilerStatus
import org.team14.webty.review.repository.ReviewRepository
import org.team14.webty.security.token.JwtManager
import org.team14.webty.user.entity.WebtyUser
import org.team14.webty.user.repository.UserRepository
import org.team14.webty.webtoon.entity.Webtoon
import org.team14.webty.webtoon.enumerate.Platform
import org.team14.webty.webtoon.repository.WebtoonRepository
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
internal class ReviewControllerTest {
    @Autowired
    private val mockMvc: MockMvc? = null

    @Autowired
    private val objectMapper: ObjectMapper? = null

    @Autowired
    private val jwtManager: JwtManager? = null

    private var jwtToken: String? = null
    private var jwtToken2: String? = null

    @Autowired
    private val userRepository: UserRepository? = null

    @Autowired
    private val reviewRepository: ReviewRepository? = null

    @Autowired
    private val webtoonRepository: WebtoonRepository? = null

    @BeforeEach
    fun setUp() {
        if (!userRepository!!.existsById(1L)) {
            // 30명의 테스트 유저 생성

            val users: MutableList<WebtyUser> = ArrayList()
            for (i in 1..30) {
                val testUser = userRepository.save(
                    WebtyUser.builder()
                        .nickname("testUser$i")
                        .profileImage("https://example.com/profile$i.jpg")
                        .build()
                )
                users.add(testUser)
            }

            // "Popular Webtoon" (1번 웹툰)
            val webtoons: MutableList<Webtoon> = ArrayList()
            val popularWebtoon = webtoonRepository!!.save(
                Webtoon(
                    webtoonName = "Popular Webtoon",
                    platform = Platform.NAVER_WEBTOON,
                    webtoonLink = "https://example.com/popular-webtoon",
                    thumbnailUrl = "https://example.com/popular-thumbnail.jpg",
                    authors = "Famous Author",
                    finished = false
                )
            )
            webtoons.add(popularWebtoon)

            for (i in 2..10) {
                val testWebtoon = webtoonRepository.save(
                    Webtoon(
                        webtoonName = "Test Webtoon $i",
                        platform = Platform.NAVER_WEBTOON,
                        webtoonLink = "https://example.com/webtoon$i",
                        thumbnailUrl = "https://example.com/thumbnail$i.jpg",
                        authors = "Test Author $i",
                        finished = (i % 2 == 0)
                    )
                )
                webtoons.add(testWebtoon)
            }

            // 유저 1번이 여러 개의 리뷰 작성 (5개)
            for (i in 1..5) {
                reviewRepository!!.save(
                    Review(
                        user = users[0], // 유저 1번
                        webtoon = popularWebtoon, // 인기 웹툰
                        title = "Review by User 1 - $i",
                        content = "This is a review written by user 1.",
                        viewCount = i * 10,
                        isSpoiler = SpoilerStatus.FALSE
                    )
                )
            }

            // 나머지 유저(2~30)는 각각 한 개의 리뷰만 작성
            for (i in 1 until users.size) {
                reviewRepository!!.save(
                    Review(
                        user = users[i],
                        webtoon = webtoons[i % webtoons.size], // 웹툰 분배
                        title = "Review by User ${i + 1}",
                        content = "This is a test review content by user ${i + 1}",
                        viewCount = (i + 1) * 5,
                        isSpoiler = SpoilerStatus.FALSE
                    )
                )
            }

            // 검색 테스트를 위한 리뷰 추가 (제목에 "Search" 포함)
            for (i in 1..3) {
                reviewRepository!!.save(
                    Review(
                        user = users[i % users.size],
                        webtoon = webtoons[i % webtoons.size],
                        title = "Search Review $i",
                        content = "This review should appear in search results.",
                        viewCount = 50 * i,
                        isSpoiler = SpoilerStatus.FALSE
                    )
                )
            }
        }
    }

    @BeforeEach
    fun setUpAuthentication() {
        val testUserId = 1L
        jwtToken = "Bearer " + jwtManager!!.createAccessToken(testUserId)
        jwtToken2 = "Bearer " + jwtManager.createAccessToken(2L)
    }

    @Test
    @DisplayName("리뷰 세부조회")
    @kotlin.Throws(Exception::class)
    fun t1() {
        mockMvc!!.perform(MockMvcRequestBuilders.get("/reviews/1"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
    }

    @Test
    @DisplayName("리뷰 세부조회 Error 존재하지 않는 리뷰")
    @kotlin.Throws(Exception::class)
    fun t2() {
        mockMvc!!.perform(MockMvcRequestBuilders.get("/reviews/53535355"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value("REVIEW-001"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("리뷰를 찾을 수 없습니다."))
    }

    @Test
    @DisplayName("리뷰 전체조회")
    @kotlin.Throws(Exception::class)
    fun t3() {
        mockMvc!!.perform(MockMvcRequestBuilders.get("/reviews"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
    }

    @Test
    @DisplayName("리뷰 생성")
    @kotlin.Throws(Exception::class)
    fun t4() {
        val request = ReviewRequest(
            webtoonId = 1L,
            title = "New Review Title",
            content = "This is a new review content.",
            spoilerStatus = SpoilerStatus.FALSE
        )

        val reviewRequestPart = MockMultipartFile(
            "reviewRequest",
            "",
            "application/json",
            objectMapper!!.writeValueAsBytes(request)
        )

        // 이미지 파일 추가
        val imageFile: MockMultipartFile = MockMultipartFile(
            "images",
            "test-image.jpg",
            "image/jpeg",
            "image-data".toByteArray()
        )

        mockMvc!!.perform(
            MockMvcRequestBuilders.multipart("/reviews/create")
                .file(reviewRequestPart) // JSON 데이터 추가
                .file(imageFile) // 이미지 파일 추가
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
    }

    @Test
    @DisplayName("리뷰 생성 Error 존재하지 않는 웹툰")
    @kotlin.Throws(Exception::class)
    fun t5() {
        val request = ReviewRequest(
            webtoonId = 12233131213L,
            title = "New Review Title",
            content = "This is a new review content.",
            spoilerStatus = SpoilerStatus.FALSE
        )

        val reviewRequestPart = MockMultipartFile(
            "reviewRequest",
            "",
            "application/json",
            objectMapper!!.writeValueAsBytes(request)
        )

        mockMvc!!.perform(
            MockMvcRequestBuilders.multipart("/reviews/create")
                .file(reviewRequestPart) // JSON 데이터 추가
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value("WEBTOON-001"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("웹툰을 찾을 수 없습니다."))
    }

    @Test
    @DisplayName("리뷰 삭제")
    @kotlin.Throws(Exception::class)
    fun t6() {
        mockMvc!!.perform(
            MockMvcRequestBuilders.delete("/reviews/delete/1")
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
    }

    @Test
    @DisplayName("리뷰 삭제 Error 존재하지 않는 리뷰")
    @kotlin.Throws(Exception::class)
    fun t7() {
        mockMvc!!.perform(
            MockMvcRequestBuilders.delete("/reviews/delete/1313131311")
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value("REVIEW-001"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("리뷰를 찾을 수 없습니다."))
    }

    @Test
    @DisplayName("리뷰 삭제 Error 권한 없음")
    @kotlin.Throws(Exception::class)
    fun t8() {
        mockMvc!!.perform(
            MockMvcRequestBuilders.delete("/reviews/delete/22")
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value("REVIEW-002"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("리뷰에 대한 삭제/수정 권한이 없습니다."))
    }

    @Test
    @DisplayName("리뷰 수정")
    @kotlin.Throws(Exception::class)
    fun t9() {
        val request = ReviewRequest(
            webtoonId = 1L,
            title = "Updated Review Title",
            content = "This is an updated review content.",
            spoilerStatus = SpoilerStatus.TRUE
        )

        val reviewRequestPart = MockMultipartFile(
            "reviewRequest",
            "",
            "application/json",
            objectMapper!!.writeValueAsBytes(request)
        )

        // 이미지 파일 추가
        val imageFile: MockMultipartFile = MockMultipartFile(
            "images",
            "updated-image.jpg",
            "image/jpeg",
            "updated-image-data".toByteArray()
        )

        mockMvc!!.perform(
            MockMvcRequestBuilders.multipart("/reviews/put/2")
                .file(reviewRequestPart) // JSON 데이터 추가
                .file(imageFile) // 이미지 파일 추가
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with { httprequest: MockHttpServletRequest ->
                    httprequest.method = "PUT" // multipart()는 기본적으로 POST라서 PUT으로 변경
                    httprequest
                })
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
    }

    @Test
    @DisplayName("리뷰 수정 Error 존재하지 않는 리뷰")
    @kotlin.Throws(Exception::class)
    fun t10() {
        val request = ReviewRequest(
            webtoonId = 1L,
            title = "Updated Review Title",
            content = "This is an updated review content.",
            spoilerStatus = SpoilerStatus.TRUE
        )

        val reviewRequestPart = MockMultipartFile(
            "reviewRequest",
            "",
            "application/json",
            objectMapper!!.writeValueAsBytes(request)
        )

        mockMvc!!.perform(
            MockMvcRequestBuilders.multipart("/reviews/put/2312424124212132")
                .file(reviewRequestPart) // JSON 데이터 추가
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with { httprequest: MockHttpServletRequest ->
                    httprequest.method = "PUT" // multipart()는 기본적으로 POST라서 PUT으로 변경
                    httprequest
                })
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value("REVIEW-001"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("리뷰를 찾을 수 없습니다."))
    }

    @Test
    @DisplayName("리뷰 수정 Error 존재하지 않는 웹툰")
    @kotlin.Throws(Exception::class)
    fun t11() {
        val request = ReviewRequest(
            webtoonId = 132142412421L,
            title = "Updated Review Title",
            content = "This is an updated review content.",
            spoilerStatus = SpoilerStatus.TRUE
        )

        val reviewRequestPart = MockMultipartFile(
            "reviewRequest",
            "",
            "application/json",
            objectMapper!!.writeValueAsBytes(request)
        )

        mockMvc!!.perform(
            MockMvcRequestBuilders.multipart("/reviews/put/2")
                .file(reviewRequestPart) // JSON 데이터 추가
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with { httprequest: MockHttpServletRequest ->
                    httprequest.method = "PUT" // multipart()는 기본적으로 POST라서 PUT으로 변경
                    httprequest
                })
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value("WEBTOON-001"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("웹툰을 찾을 수 없습니다."))
    }

    @Test
    @DisplayName("리뷰 수정 Error 권한 없음")
    @kotlin.Throws(Exception::class)
    fun t12() {
        val request = ReviewRequest(
            webtoonId = 1L,
            title = "Updated Review Title",
            content = "This is an updated review content.",
            spoilerStatus = SpoilerStatus.TRUE
        )

        val reviewRequestPart = MockMultipartFile(
            "reviewRequest",
            "",
            "application/json",
            objectMapper!!.writeValueAsBytes(request)
        )

        mockMvc!!.perform(
            MockMvcRequestBuilders.multipart("/reviews/put/2")
                .file(reviewRequestPart) // JSON 데이터 추가
                .header(HttpHeaders.AUTHORIZATION, jwtToken2)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with { httprequest: MockHttpServletRequest ->
                    httprequest.method = "PUT" // multipart()는 기본적으로 POST라서 PUT으로 변경
                    httprequest
                })
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value("REVIEW-002"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("리뷰에 대한 삭제/수정 권한이 없습니다."))
    }


    @Test
    @DisplayName("자신이 작성한 리뷰 조회")
    @kotlin.Throws(Exception::class)
    fun t13() {
        mockMvc!!.perform(
            MockMvcRequestBuilders.get("/reviews/me")
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
    }

    @Test
    @DisplayName("조회수 순 리뷰 조회")
    @kotlin.Throws(Exception::class)
    fun t14() {
        mockMvc!!.perform(MockMvcRequestBuilders.get("/reviews/view-count-desc"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
    }

    @Test
    @DisplayName("접속한 유저가 작성한 리뷰 개수 조회")
    @kotlin.Throws(Exception::class)
    fun t15() {
        mockMvc!!.perform(
            MockMvcRequestBuilders.get("/reviews/me/count")
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
    }

    @Test
    @DisplayName("리뷰 제목명 검색")
    @kotlin.Throws(Exception::class)
    fun t16() {
        mockMvc!!.perform(MockMvcRequestBuilders.get("/reviews/search").param("title", "User 1 -"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
    }

    @Test
    @DisplayName("웹툰 ID에 해당하는 리뷰 조회")
    @kotlin.Throws(Exception::class)
    fun t17() {
        mockMvc!!.perform(MockMvcRequestBuilders.get("/reviews/webtoon/1"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
    }

    @Test
    @DisplayName("리뷰 ID에 해당하는 리뷰 스포일러 처리")
    @kotlin.Throws(Exception::class)
    fun t18() {
        mockMvc!!.perform(MockMvcRequestBuilders.patch("/reviews/spoiler/2"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
    }

    @Test
    @DisplayName("리뷰 ID에 해당하는 리뷰 스포일러 처리 Error 존재하지않는 리뷰 ID")
    @kotlin.Throws(
        Exception::class
    )
    fun t19() {
        mockMvc!!.perform(MockMvcRequestBuilders.patch("/reviews/spoiler/2123213214241224"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value("REVIEW-001"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("리뷰를 찾을 수 없습니다."))
    }
}
