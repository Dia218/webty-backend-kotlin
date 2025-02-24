package org.team14.webty.reviewComment.controller

import org.junit.jupiter.api.BeforeEach  // 각 테스트 전에 실행될 메소드 지정
import org.junit.jupiter.api.DisplayName  // 테스트 이름을 지정하기 위한 어노테이션
import org.junit.jupiter.api.Test        // 테스트 메소드임을 나타내는 어노테이션
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional
import org.springframework.test.context.TestPropertySource
import org.springframework.test.annotation.DirtiesContext
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
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

// 스프링 부트 테스트 환경을 설정하는 어노테이션
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
// 테스트용 프로퍼티 설정 (test 프로필 활성화)
@TestPropertySource(properties = ["spring.profiles.active=test"])
// 각 테스트 메소드를 트랜잭션으로 감싸는 어노테이션
@Transactional
// 각 테스트 메소드 실행 전에 새로운 ApplicationContext를 생성
class ReviewCommentControllerTest {

    // MockMvc 객체 주입 (HTTP 요청을 시뮬레이션하기 위한 객체)
    @Autowired
    private lateinit var mockMvc: MockMvc

    // JSON 변환을 위한 ObjectMapper 주입
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    // 테스트에 필요한 레포지토리들 주입
    @Autowired
    private lateinit var userRepository: UserRepository//사용자 데이터 처리

    @Autowired
    private lateinit var webtoonRepository: WebtoonRepository//웹툰 데이터 처리

    @Autowired
    private lateinit var reviewRepository: ReviewRepository//리뷰 데이터 처리

    @Autowired
    private lateinit var reviewCommentRepository: ReviewCommentRepository//댓글 데이터 처리

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
        runCatching {
            // 테스트 데이터 정리
            reviewCommentRepository.deleteAllInBatch() // 댓글 데이터 삭제
            reviewRepository.deleteAllInBatch() // 리뷰 데이터 삭제
            webtoonRepository.deleteAllInBatch() // 웹툰 데이터 삭제
            userRepository.deleteAllInBatch() // 사용자 데이터 삭제

            testUser = userRepository.saveAndFlush( // 테스트 사용자 저장
                WebtyUser(
                    nickname = "테스트유저",
                    profileImage = "testImage"
                )
            )

            testWebtoon = webtoonRepository.saveAndFlush( // 테스트 웹툰 저장
                Webtoon(
                    webtoonName = "테스트 웹툰",
                    platform = Platform.NAVER_WEBTOON,
                    webtoonLink = "https://test.com",
                    thumbnailUrl = "https://test.com/thumb.jpg",
                    authors = "테스트 작가",
                    finished = false
                )
            )

            testReview = reviewRepository.saveAndFlush( // 테스트 리뷰 저장
                Review(
                    user = testUser,
                    webtoon = testWebtoon,
                    title = "테스트 리뷰",
                    content = "테스트 내용",
                    isSpoiler = SpoilerStatus.FALSE
                )
            )

            jwtToken = "Bearer ${jwtManager.createAccessToken(testUser.userId!!)}" // JWT 토큰 생성
        }.onFailure { e ->
            println("Setup failed: ${e.message}") // 예외 처리 출력
            throw e
        }.getOrThrow()
    }

    // 각 테스트 실행 후 실행되는 테스트 데이터 정리 메소드
    @AfterEach
    fun tearDown() {
        runCatching {
            reviewCommentRepository.deleteAllInBatch() // 댓글 데이터 삭제
            reviewRepository.deleteAllInBatch() // 리뷰 데이터 삭제
            webtoonRepository.deleteAllInBatch() // 웹툰 데이터 삭제
            userRepository.deleteAllInBatch() // 사용자 데이터 삭제
        }.onFailure { e ->
            println("Cleanup failed: ${e.message}") // 예외 처리 출력
            throw e
        }.getOrThrow()
    }

    // 댓글 생성 테스트
    @Test
    @DisplayName("댓글 생성")
    fun t1() {
        runCatching {
            val request = CommentRequest(
                content = "테스트 댓글",
                mentions = listOf(),
                parentCommentId = null
            )

            mockMvc.perform(
                post("/reviews/${testReview.reviewId}/comments") // 리뷰 ID에 댓글 생성 요청
                    .header("Authorization", jwtToken) // 인증 토큰 설정
                    .contentType(MediaType.APPLICATION_JSON) // JSON 형식 지정
                    .content(objectMapper.writeValueAsString(request)) // 요청 본문 설정
            )
                .andDo(print()) // 요청/응답 내용 출력
                .andExpect(status().isOk) // HTTP 상태코드 200 확인
                .andExpect(jsonPath("$.content").value("테스트 댓글")) // 응답 내용 검증
        }.onFailure { e ->
            println("댓글 생성 테스트 실패: ${e.message}") // 예외 처리 출력
            throw e
        }.getOrThrow()
    }

    @Test
    @DisplayName("댓글 수정")
    fun t2() = runCatching {
        // 수정할 테스트용 댓글 생성 및 저장
        val comment = reviewCommentRepository.saveAndFlush( // 댓글 저장
            org.team14.webty.reviewComment.entity.ReviewComment( // 댓글 엔티티 생성
                user = testUser, // 댓글 작성자
                review = testReview, // 댓글 속한 리뷰
                content = "원본 댓글" // 댓글 내용
            )
        )

        // 수정 요청 객체 생성
        val request = CommentRequest( // 수정 요청 객체 생성
            content = "수정된 댓글", // 수정된 댓글 내용
            mentions = listOf(), // 멘션 목록
            parentCommentId = null // 부모 댓글 ID
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
    }.onFailure { e ->
        // 예외 처리 출력
        println("댓글 수정 테스트 실패: ${e.message}")
        throw e
    }

    @Test
    @DisplayName("댓글 삭제")
    fun t3() = runCatching {
        val comment = reviewCommentRepository.saveAndFlush( // 댓글 저장
            org.team14.webty.reviewComment.entity.ReviewComment( // 댓글 엔티티 생성
                user = testUser, // 댓글 작성자
                review = testReview, // 댓글 속한 리뷰
                content = "삭제될 댓글" // 댓글 내용
            )
        )

        // MockMvc를 사용하여 댓글 삭제(DELETE) 요청 실행
        mockMvc.perform(
            delete("/reviews/${testReview.reviewId}/comments/${comment.commentId}")
                .header("Authorization", jwtToken)  // 인증 토큰 설정
        )
            .andDo(print()) // 요청/응답 내용 출력
            .andExpect(status().isOk) // HTTP 상태코드 200 확인
    }.onFailure { e ->
        // 예외 처리 출력
        println("댓글 삭제 테스트 실패: ${e.message}")
        throw e
    }

    @Test
    @DisplayName("댓글 목록 조회")
    fun t4() = runCatching {
        // MockMvc를 사용하여 댓글 목록 조회(GET) 요청 실행
        mockMvc.perform(
            get("/reviews/${testReview.reviewId}/comments")
                .param("page", "0")  // 페이지 번호 설정
                .param("size", "10")  // 페이지 크기 설정
        )
            .andDo(print()) // 요청/응답 내용 출력
            .andExpect(status().isOk) // HTTP 상태코드 200 확인
    }.onFailure { e ->
        // 예외 처리 출력
        println("댓글 목록 조회 테스트 실패: ${e.message}")
        throw e
    }
    
    //예외 케이스 테스트 1
    @Test
    @DisplayName("존재하지 않는 리뷰의 댓글 생성 시도")
    fun t5() = runCatching {
        val request = CommentRequest( // 댓글 생성 요청 객체 생성
            content = "테스트 댓글", // 댓글 내용
            mentions = listOf(), // 멘션 목록
            parentCommentId = null // 부모 댓글 ID
        )

        // MockMvc를 사용하여 댓글 생성(POST) 요청 실행
        mockMvc.perform(
            post("/reviews/99999/comments") // 존재하지 않는 리뷰 ID로 요청
                .header("Authorization", jwtToken)  // 인증 토큰 설정
                .contentType(MediaType.APPLICATION_JSON)  // JSON 형식 지정
                .content(objectMapper.writeValueAsString(request))  // 요청 본문 설정
        )
            .andDo(print()) // 요청/응답 내용 출력
            .andExpect(status().isNotFound) // HTTP 상태코드 404 확인
    }.onFailure { e ->
        // 예외 처리 출력
        println("존재하지 않는 리뷰 댓글 생성 테스트 실패: ${e.message}")
        throw e
    }

    //예외 케이스 테스트 2
    @Test
    @DisplayName("권한 없는 댓글 수정 시도")
    fun t6() = runCatching {
        val otherUser = userRepository.saveAndFlush( // 다른 사용자 저장
            WebtyUser(
                nickname = "다른 사용자", // 닉네임
                profileImage = "otherImage" // 프로필 이미지
            )
        )

        // 다른 사용자의 댓글 생성 및 저장
        val comment = reviewCommentRepository.saveAndFlush( // 댓글 저장
            org.team14.webty.reviewComment.entity.ReviewComment( // 댓글 엔티티 생성
                user = otherUser, // 댓글 작성자
                review = testReview, // 댓글 속한 리뷰
                content = "다른 사용자의 댓글" // 댓글 내용
            )
        )

        // 수정 요청 객체 생성
        val request = CommentRequest( // 수정 요청 객체 생성
            content = "수정 시도", // 수정된 댓글 내용
            mentions = listOf(), // 멘션 목록
            parentCommentId = null // 부모 댓글 ID
        )

        // MockMvc를 사용하여 댓글 수정(PUT) 요청 실행
        mockMvc.perform(
            put("/reviews/${testReview.reviewId}/comments/${comment.commentId}")
                .header("Authorization", jwtToken)  // 인증 토큰 설정
                .contentType(MediaType.APPLICATION_JSON)  // JSON 형식 지정
                .content(objectMapper.writeValueAsString(request))  // 요청 본문 설정
        )
            .andDo(print()) // 요청/응답 내용 출력
            .andExpect(status().isUnauthorized) // HTTP 상태코드 401 확인
    }.onFailure { e ->
        // 예외 처리 출력
        println("권한 없는 댓글 수정 테스트 실패: ${e.message}")
        throw e
    }
}