package org.team14.webty.reviewComment.controller

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import org.team14.webty.review.entity.Review
import org.team14.webty.review.enumrate.SpoilerStatus
import org.team14.webty.review.repository.ReviewRepository
import org.team14.webty.reviewComment.dto.CommentRequest
import org.team14.webty.reviewComment.repository.ReviewCommentRepository
import org.team14.webty.security.token.JwtManager
import org.team14.webty.user.entity.WebtyUser
import org.team14.webty.user.repository.UserRepository
import org.team14.webty.webtoon.entity.Webtoon
import org.team14.webty.webtoon.enumerate.Platform
import org.team14.webty.webtoon.repository.WebtoonRepository
import com.fasterxml.jackson.databind.ObjectMapper

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = ["spring.profiles.active=test"])
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ReviewCommentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var webtoonRepository: WebtoonRepository

    @Autowired
    private lateinit var reviewRepository: ReviewRepository

    @Autowired
    private lateinit var reviewCommentRepository: ReviewCommentRepository

    @Autowired
    private lateinit var jwtManager: JwtManager

    private lateinit var testUser: WebtyUser
    private lateinit var testWebtoon: Webtoon
    private lateinit var testReview: Review
    private lateinit var jwtToken: String

    @BeforeEach
    fun setUp() {
        // 기존 데이터 정리
        reviewCommentRepository.deleteAll()
        reviewRepository.deleteAll()
        webtoonRepository.deleteAll()
        userRepository.deleteAll()

        // 테스트 유저 생성
        testUser = userRepository.save(
            WebtyUser(
                nickname = "테스트유저",
                profileImage = "testImage"
            )
        )

        // 테스트 웹툰 생성
        testWebtoon = webtoonRepository.save(
            Webtoon(
                webtoonName = "테스트 웹툰",
                platform = Platform.NAVER_WEBTOON,
                webtoonLink = "https://test.com",
                thumbnailUrl = "https://test.com/thumb.jpg",
                authors = "테스트 작가",
                finished = false
            )
        )

        // 테스트 리뷰 생성
        testReview = reviewRepository.save(
            Review(
                user = testUser,
                webtoon = testWebtoon,
                title = "테스트 리뷰",
                content = "테스트 내용",
                isSpoiler = SpoilerStatus.FALSE
            )
        )

        // JWT 토큰 생성
        jwtToken = "Bearer " + jwtManager.createAccessToken(testUser.userId!!)
    }

    @Test
    @DisplayName("댓글 생성")
    fun t1() {
        val request = CommentRequest(
            content = "테스트 댓글",
            mentions = listOf(),
            parentCommentId = null
        )

        mockMvc.perform(
            post("/reviews/${testReview.reviewId}/comments")
                .header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").value("테스트 댓글"))
    }

    @Test
    @DisplayName("댓글 수정")
    fun t2() {
        val comment = reviewCommentRepository.save(
            org.team14.webty.reviewComment.entity.ReviewComment(
                user = testUser,
                review = testReview,
                content = "원본 댓글"
            )
        )

        val request = CommentRequest(
            content = "수정된 댓글",
            mentions = listOf(),
            parentCommentId = null
        )

        mockMvc.perform(
            put("/reviews/${testReview.reviewId}/comments/${comment.commentId}")
                .header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").value("수정된 댓글"))
    }

    @Test
    @DisplayName("댓글 삭제")
    fun t3() {
        val comment = reviewCommentRepository.save(
            org.team14.webty.reviewComment.entity.ReviewComment(
                user = testUser,
                review = testReview,
                content = "삭제될 댓글"
            )
        )

        mockMvc.perform(
            delete("/reviews/${testReview.reviewId}/comments/${comment.commentId}")
                .header("Authorization", jwtToken)
        )
            .andDo(print())
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("댓글 목록 조회")
    fun t4() {
        mockMvc.perform(
            get("/reviews/${testReview.reviewId}/comments")
                .param("page", "0")
                .param("size", "10")
        )
            .andDo(print())
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("존재하지 않는 리뷰의 댓글 생성 시도")
    fun t5() {
        val request = CommentRequest(
            content = "테스트 댓글",
            mentions = listOf(),
            parentCommentId = null
        )

        mockMvc.perform(
            post("/reviews/99999/comments")
                .header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("권한 없는 댓글 수정 시도")
    fun t6() {
        val otherUser = userRepository.save(
            WebtyUser(
                nickname = "다른 사용자",
                profileImage = "otherImage"
            )
        )

        val comment = reviewCommentRepository.save(
            org.team14.webty.reviewComment.entity.ReviewComment(
                user = otherUser,
                review = testReview,
                content = "다른 사용자의 댓글"
            )
        )

        val request = CommentRequest(
            content = "수정 시도",
            mentions = listOf(),
            parentCommentId = null
        )

        mockMvc.perform(
            put("/reviews/${testReview.reviewId}/comments/${comment.commentId}")
                .header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isUnauthorized)
    }
} 