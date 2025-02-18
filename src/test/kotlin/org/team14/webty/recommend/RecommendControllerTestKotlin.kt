package org.team14.webty.recommend

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.team14.webty.recommend.repository.RecommendRepository
import org.team14.webty.review.entity.Review
import org.team14.webty.review.enumrate.SpoilerStatus
import org.team14.webty.review.repository.ReviewRepository
import org.team14.webty.security.token.JwtManager
import org.team14.webty.user.entity.SocialProvider
import org.team14.webty.user.entity.WebtyUser
import org.team14.webty.user.enumerate.SocialProviderType
import org.team14.webty.user.repository.UserRepository
import org.team14.webty.webtoon.entity.Webtoon
import org.team14.webty.webtoon.enumerate.Platform
import org.team14.webty.webtoon.repository.WebtoonRepository
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = ["spring.profiles.active=test"])
internal class RecommendControllerTestKotlin {

    @Autowired
    private var mockMvc: MockMvc? = null

    @Autowired
    private val reviewRepository: ReviewRepository? = null

    @Autowired
    private val userRepository: UserRepository? = null

    @Autowired
    private val webtoonRepository: WebtoonRepository? = null

    @Autowired
    private val recommendRepository: RecommendRepository? = null

    @Autowired
    private val jwtManager: JwtManager? = null
    private var testUser: WebtyUser? = null
    private var testReview: Review? = null

    @BeforeEach
    fun beforeEach() {
        testUser = userRepository?.save(
            WebtyUser.builder()
                .nickname("테스트유저")
                .profileImage("dasdsa")
                .socialProvider(
                    SocialProvider.builder()
                        .provider(SocialProviderType.KAKAO)
                        .providerId("313213231")
                        .build()
                )
                .build()
        )

        val testWebtoon = webtoonRepository?.save(
            Webtoon.builder()
                .webtoonName("테스트 웹툰")
                .platform(Platform.KAKAO_PAGE)
                .webtoonLink("www.abc")
                .thumbnailUrl("www.bcd")
                .authors("testtest")
                .finished(true)
                .build()
        )
        testReview = reviewRepository?.save(
            Review(
                user = testUser!!,
                content = "테스트 리뷰",
                title = "테스트 리뷰 제목",
                viewCount = 0,
                isSpoiler = SpoilerStatus.FALSE,
                webtoon = testWebtoon!!,
                createdAt = LocalDateTime.now())
        )
    }

    @AfterEach
    fun afterEach() {
        recommendRepository!!.deleteAll()
        reviewRepository!!.deleteAll()
        webtoonRepository!!.deleteAll()
        userRepository!!.deleteAll()
    }

    @Test
    @DisplayName("추천 테스트")
    @Throws(Exception::class)
    fun t1() {
        val reviewId = testReview!!.reviewId
        val type = "like"
        val accessToken = jwtManager!!.createAccessToken(testUser!!.userId)

        mockMvc!!.perform(
            MockMvcRequestBuilders.post("/recommend/$reviewId")
                .header("Authorization", "Bearer $accessToken")
                .param("type", type)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().string("1"))
    }

    @Test
    @DisplayName("추천 중복 테스트")
    @Throws(Exception::class)
    fun t2() {
        val reviewId = testReview!!.reviewId
        val type = "like"
        val accessToken = jwtManager!!.createAccessToken(testUser!!.userId)

        mockMvc!!.perform(
            MockMvcRequestBuilders.post("/recommend/$reviewId")
                .header("Authorization", "Bearer $accessToken")
                .param("type", type)
        )

        mockMvc!!.perform(
            MockMvcRequestBuilders.post("/recommend/$reviewId")
                .header("Authorization", "Bearer $accessToken")
                .param("type", type)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("추천/비추천을 두번 이상 할 수 없습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value("RECOMMEND-001"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value("BAD_REQUEST"))
    }

    @Test
    @DisplayName("추천 테스트 with 이상한 type")
    @Throws(Exception::class)
    fun t3() {
        val reviewId = testReview!!.reviewId
        val type = "abb"
        val accessToken = jwtManager!!.createAccessToken(testUser!!.userId)

        mockMvc!!.perform(
            MockMvcRequestBuilders.post("/recommend/$reviewId")
                .header("Authorization", "Bearer $accessToken")
                .param("type", type)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest()) // 400 상태
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON)) // JSON 응답 확인
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("type은 LIKE(like), HATE(hate)만 가능합니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value("RECOMMEND-002"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value("BAD_REQUEST"))
    }

    @Test
    @DisplayName("추천 취소 테스트")
    @Throws(Exception::class)
    fun t4() {
        val reviewId = testReview!!.reviewId
        val type = "like"
        val accessToken = jwtManager!!.createAccessToken(testUser!!.userId)

        mockMvc!!.perform(
            MockMvcRequestBuilders.post("/recommend/$reviewId")
                .header("Authorization", "Bearer $accessToken")
                .param("type", type)
        )

        mockMvc!!.perform(
            MockMvcRequestBuilders.delete("/recommend/$reviewId")
                .header("Authorization", "Bearer $accessToken")
                .param("type", type)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
    }

    @Test
    @DisplayName("추천 취소 테스트 with 존재하지 않는 추천")
    @Throws(Exception::class)
    fun t5() {
        val reviewId = testReview!!.reviewId
        val type = "like"
        val accessToken = jwtManager!!.createAccessToken(testUser!!.userId)

        mockMvc!!.perform(
            MockMvcRequestBuilders.delete("/recommend/$reviewId")
                .header("Authorization", "Bearer $accessToken")
                .param("type", type)
        )
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("해당 추천/비추천이 존재하지 않습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value("RECOMMEND-003"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value("NOT_FOUND"))
    }

    @Test
    @DisplayName("로그인한 사용자 추천 리뷰 목록 조회")
    @Throws(Exception::class)
    fun t6() {
        val userId = testUser!!.userId
        val reviewId = testReview!!.reviewId
        val type = "like"
        val accessToken = jwtManager!!.createAccessToken(testUser!!.userId)

        mockMvc!!.perform(
            MockMvcRequestBuilders.post("/recommend/$reviewId")
                .header("Authorization", "Bearer $accessToken")
                .param("type", type)
        )

        mockMvc!!.perform(
            MockMvcRequestBuilders.get("/recommend/user/$userId")
                .header("Authorization", "Bearer $accessToken")
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].reviewId").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].title").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].recommendCount").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext").isBoolean())
            .andExpect(MockMvcResultMatchers.jsonPath("$.hasPrevious").isBoolean())
            .andExpect(MockMvcResultMatchers.jsonPath("$.isLast").isBoolean())
    }

    @Test
    @DisplayName("선택 리뷰 추천 상태")
    @Throws(Exception::class)
    fun t7() {
        val reviewId = testReview!!.reviewId
        val type = "like"
        val accessToken = jwtManager!!.createAccessToken(testUser!!.userId)

        mockMvc!!.perform(
            MockMvcRequestBuilders.post("/recommend/$reviewId")
                .header("Authorization", "Bearer $accessToken")
                .param("type", type)

        )

        mockMvc!!.perform(
            MockMvcRequestBuilders.get("/recommend/$reviewId/recommendation")
                .header("Authorization", "Bearer $accessToken")

        )
            .andExpect(MockMvcResultMatchers.jsonPath("$.LIKES").isBoolean())
            .andExpect(MockMvcResultMatchers.jsonPath("$.LIKES").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.HATES").isBoolean())
            .andExpect(MockMvcResultMatchers.jsonPath("$.HATES").value(false))
    }
}