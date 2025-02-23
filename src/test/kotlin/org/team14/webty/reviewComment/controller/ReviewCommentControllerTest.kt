package org.team14.webty.reviewComment.controller

import org.junit.jupiter.api.BeforeEach  // 각 테스트 전에 실행될 메소드 지정
import org.junit.jupiter.api.DisplayName  // 테스트 이름을 지정하기 위한 어노테이션
import org.junit.jupiter.api.Test        // 테스트 메소드임을 나타내는 어노테이션
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
import org.junit.jupiter.api.AfterEach

// 스프링 부트 테스트 환경을 설정하는 어노테이션
@SpringBootTest
// MockMvc를 자동으로 설정하는 어노테이션
@AutoConfigureMockMvc
// 테스트용 프로퍼티 설정 (test 프로필 활성화)
@TestPropertySource(properties = ["spring.profiles.active=test"])
// 각 테스트 메소드를 트랜잭션으로 감싸는 어노테이션
@Transactional
// 각 테스트 메소드 실행 전에 새로운 ApplicationContext를 생성
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReviewCommentControllerTest {


    // MockMvc 객체 주입 (HTTP 요청을 시뮬레이션하기 위한 객체)
    @Autowired
    private lateinit var mockMvc: MockMvc

    // JSON 변환을 위한 ObjectMapper 주입
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    // 테스트에 필요한 레포지토리들 주입
    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var webtoonRepository: WebtoonRepository

    @Autowired
    private lateinit var reviewRepository: ReviewRepository

    @Autowired
    private lateinit var reviewCommentRepository: ReviewCommentRepository

    // JWT 토큰 생성을 위한 매니저 주입
    @Autowired
    private lateinit var jwtManager: JwtManager

    // 테스트에 사용될 엔티티들을 저장할 변수 선언
    private lateinit var testUser: WebtyUser
    private lateinit var testWebtoon: Webtoon
    private lateinit var testReview: Review
    private lateinit var jwtToken: String

    // 각 테스트 실행 전에 실행되는 설정 메소드
    @BeforeEach
    fun setUp() {
        try {
            // 기존 데이터를 모두 삭제 (역순으로 삭제하여 참조 무결성 유지)
            reviewCommentRepository.deleteAllInBatch()
            reviewRepository.deleteAllInBatch()
            webtoonRepository.deleteAllInBatch()
            userRepository.deleteAllInBatch()

            // 테스트 유저 생성
            testUser = WebtyUser(
                nickname = "테스트유저",
                profileImage = "testImage"
            )
            // 생성한 사용자를 DB에 저장하고 즉시 반영
            testUser = userRepository.saveAndFlush(testUser)

            // 테스트 웹툰 생성
            testWebtoon = Webtoon(
                webtoonName = "테스트 웹툰",
                platform = Platform.NAVER_WEBTOON,
                webtoonLink = "https://test.com",
                thumbnailUrl = "https://test.com/thumb.jpg",
                authors = "테스트 작가",
                finished = false
            )
            // 생성한 웹툰을 DB에 저장하고 즉시 반영
            testWebtoon = webtoonRepository.saveAndFlush(testWebtoon)

            // 테스트 리뷰 생성
            testReview = Review(
                user = testUser,
                webtoon = testWebtoon,
                title = "테스트 리뷰",
                content = "테스트 내용",
                isSpoiler = SpoilerStatus.FALSE
            )
            // 생성한 리뷰를 DB에 저장하고 즉시 반영
            testReview = reviewRepository.saveAndFlush(testReview)

            // 테스트용 JWT 토큰 생성
            jwtToken = "Bearer " + jwtManager.createAccessToken(testUser.userId!!)
        } catch (e: Exception) {
            // 설정 오류 발생 시 로그 출력 후 예외처리
            println("Setup failed: ${e.message}")
            throw e
        }
    }

    // 각 테스트 실행 후 실행되는 정리 메소드
    @AfterEach
    fun tearDown() {
        try {
            // 생성한 데이터를 모두 삭제 (역순으로 삭제하여 참조 무결성 유지)
            reviewCommentRepository.deleteAllInBatch()
            reviewRepository.deleteAllInBatch()
            webtoonRepository.deleteAllInBatch()
            userRepository.deleteAllInBatch()
        } catch (e: Exception) {
            // 정리 작업 중 오류 발생 시 로그 출력
            println("Cleanup failed: ${e.message}")
        }
    }

    @Test
    @DisplayName("댓글 생성")
    fun t1() {
        // 테스트용 댓글 생성 요청 객체 생성
        val request = CommentRequest(
            content = "테스트 댓글",
            mentions = listOf(),
            parentCommentId = null
        )

        // MockMvc를 사용하여 댓글 생성(POST) 요청 실행
        mockMvc.perform(
            post("/reviews/${testReview.reviewId}/comments")
                .header("Authorization", jwtToken)  // 인증 토큰 설정
                .contentType(MediaType.APPLICATION_JSON)  // JSON 형식 지정
                .content(objectMapper.writeValueAsString(request))  // 요청 본문 설정
        )
            .andDo(print())  // 요청/응답 내용 출력
            .andExpect(status().isOk)  // HTTP 상태코드 200 확인
            .andExpect(jsonPath("$.content").value("테스트 댓글"))  // 응답 내용 검증
    }

    @Test
    @DisplayName("댓글 수정")
    fun t2() {
        // 수정할 테스트용 댓글 생성 및 저장
        val comment = reviewCommentRepository.save(
            org.team14.webty.reviewComment.entity.ReviewComment(
                user = testUser,
                review = testReview,
                content = "원본 댓글"
            )
        )

        // 수정 요청 객체 생성
        val request = CommentRequest(
            content = "수정된 댓글",
            mentions = listOf(),
            parentCommentId = null
        )

        // MockMvc를 사용하여 댓글 수정(PUT) 요청 실행
        mockMvc.perform(
            put("/reviews/${testReview.reviewId}/comments/${comment.commentId}")
                .header("Authorization", jwtToken)  // 인증 토큰 설정
                .contentType(MediaType.APPLICATION_JSON)  // JSON 형식 지정
                .content(objectMapper.writeValueAsString(request))  // 요청 본문 설정
        )
            .andDo(print())  // 요청/응답 내용 출력
            .andExpect(status().isOk)  // HTTP 상태코드 200 확인
            .andExpect(jsonPath("$.content").value("수정된 댓글"))  // 응답 내용 검증
    }

    @Test
    @DisplayName("댓글 삭제")
    fun t3() {
        // 삭제할 테스트용 댓글 생성 및 저장
        val comment = reviewCommentRepository.save(
            org.team14.webty.reviewComment.entity.ReviewComment(
                user = testUser,
                review = testReview,
                content = "삭제될 댓글"
            )
        )

        // MockMvc를 사용하여 댓글 삭제(DELETE) 요청 실행
        mockMvc.perform(
            delete("/reviews/${testReview.reviewId}/comments/${comment.commentId}")
                .header("Authorization", jwtToken)  // 인증 토큰 설정
        )
            .andDo(print())  // 요청/응답 내용 출력
            .andExpect(status().isOk)  // HTTP 상태코드 200 확인
    }

    @Test
    @DisplayName("댓글 목록 조회")
    fun t4() {
        // MockMvc를 사용하여 댓글 목록 조회(GET) 요청 실행
        mockMvc.perform(
            get("/reviews/${testReview.reviewId}/comments")
                .param("page", "0")  // 페이지 번호 설정
                .param("size", "10")  // 페이지 크기 설정
        )
            .andDo(print())  // 요청/응답 내용 출력
            .andExpect(status().isOk)  // HTTP 상태코드 200 확인
    }

    @Test
    @DisplayName("존재하지 않는 리뷰의 댓글 생성 시도")
    fun t5() {
        // 존재하지 않는 리뷰의 댓글 생성 요청 객체 생성
        val request = CommentRequest(
            content = "테스트 댓글",
            mentions = listOf(),
            parentCommentId = null
        )

        // MockMvc를 사용하여 댓글 생성(POST) 요청 실행
        mockMvc.perform(
            post("/reviews/99999/comments")
                .header("Authorization", jwtToken)  // 인증 토큰 설정
                .contentType(MediaType.APPLICATION_JSON)  // JSON 형식 지정
                .content(objectMapper.writeValueAsString(request))  // 요청 본문 설정
        )
            .andDo(print())  // 요청/응답 내용 출력
            .andExpect(status().isNotFound)  // HTTP 상태코드 404 확인
    }

    @Test
    @DisplayName("권한 없는 댓글 수정 시도")
    fun t6() {
        // 다른 사용자 생성 및 저장
        val otherUser = userRepository.save(
            WebtyUser(
                nickname = "다른 사용자",
                profileImage = "otherImage"
            )
        )

        // 다른 사용자의 댓글 생성 및 저장
        val comment = reviewCommentRepository.save(
            org.team14.webty.reviewComment.entity.ReviewComment(
                user = otherUser,
                review = testReview,
                content = "다른 사용자의 댓글"
            )
        )

        // 수정 요청 객체 생성
        val request = CommentRequest(
            content = "수정 시도",
            mentions = listOf(),
            parentCommentId = null
        )

        // MockMvc를 사용하여 댓글 수정(PUT) 요청 실행
        mockMvc.perform(
            put("/reviews/${testReview.reviewId}/comments/${comment.commentId}")
                .header("Authorization", jwtToken)  // 인증 토큰 설정
                .contentType(MediaType.APPLICATION_JSON)  // JSON 형식 지정
                .content(objectMapper.writeValueAsString(request))  // 요청 본문 설정
        )
            .andDo(print())  // 요청/응답 내용 출력
            .andExpect(status().isUnauthorized)  // HTTP 상태코드 401 확인
    }
} 