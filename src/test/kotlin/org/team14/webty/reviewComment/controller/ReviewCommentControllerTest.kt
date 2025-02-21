// 리뷰 댓글 컨트롤러의 API 엔드포인트들을 테스트하는 파일
package org.team14.webty.reviewComment.controller

// 테스트 관련 import
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName

// 스프링 테스트 관련 import
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.web.util.UriComponentsBuilder

// 엔티티 및 레포지토리 import
import org.team14.webty.review.entity.Review
import org.team14.webty.review.enumrate.SpoilerStatus
import org.team14.webty.review.repository.ReviewRepository
import org.team14.webty.reviewComment.dto.CommentRequest
import org.team14.webty.reviewComment.entity.ReviewComment
import org.team14.webty.reviewComment.repository.ReviewCommentRepository
import org.team14.webty.security.token.JwtManager
import org.team14.webty.user.entity.SocialProvider
import org.team14.webty.user.entity.WebtyUser
import org.team14.webty.user.enumerate.SocialProviderType
import org.team14.webty.user.repository.UserRepository
import org.team14.webty.webtoon.entity.Webtoon
import org.team14.webty.webtoon.enumerate.Platform
import org.team14.webty.webtoon.repository.WebtoonRepository

// 유틸리티 import
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.assertj.core.api.Assertions.assertThat
import jakarta.persistence.EntityManager

// 리뷰 댓글 컨트롤러의 API 엔드포인트들을 테스트하는 클래스
// 통합 테스트 환경에서 실제 DB 연동과 HTTP 요청/응답을 테스트합니다.
// TestEntityFactory를 사용하여 테스트 데이터를 생성하고 검증합니다.

class TestEntityFactory {
    companion object {
        fun socialProvider(init: SocialProviderBuilder.() -> Unit): SocialProvider =
            SocialProviderBuilder().apply(init).build()

        fun webtyUser(init: WebtyUserBuilder.() -> Unit): WebtyUser =
            WebtyUserBuilder().apply(init).build()

        fun webtoon(init: WebtoonBuilder.() -> Unit): Webtoon =
            WebtoonBuilder().apply(init).build()

        fun review(init: ReviewBuilder.() -> Unit): Review =
            ReviewBuilder().apply(init).build()

        fun reviewComment(init: ReviewCommentBuilder.() -> Unit): ReviewComment =
            ReviewCommentBuilder().apply(init).build()
    }

    class SocialProviderBuilder {
        var provider: SocialProviderType = SocialProviderType.KAKAO
        var providerId: String = "313213231"

        fun build(): SocialProvider = SocialProvider::class.java.getDeclaredConstructor().let { constructor ->
            constructor.isAccessible = true
            constructor.newInstance().also { socialProvider ->
                SocialProvider::class.java.getDeclaredField("provider").let { field ->
                    field.isAccessible = true
                    field.set(socialProvider, provider)
                }
                SocialProvider::class.java.getDeclaredField("providerId").let { field ->
                    field.isAccessible = true
                    field.set(socialProvider, providerId)
                }
            }
        }
    }

    class WebtyUserBuilder {
        var nickname: String = "테스트유저"
        var profileImage: String = "dasdsa"
        lateinit var socialProvider: SocialProvider

        fun build(): WebtyUser = WebtyUser::class.java.getDeclaredConstructor().let { constructor ->
            constructor.isAccessible = true
            constructor.newInstance().also { user ->
                WebtyUser::class.java.getDeclaredField("nickname").let { field ->
                    field.isAccessible = true
                    field.set(user, nickname)
                }
                WebtyUser::class.java.getDeclaredField("profileImage").let { field ->
                    field.isAccessible = true
                    field.set(user, profileImage)
                }
                WebtyUser::class.java.getDeclaredField("socialProvider").let { field ->
                    field.isAccessible = true
                    field.set(user, socialProvider)
                }
            }
        }
    }

    class WebtoonBuilder {
        var webtoonName: String = "테스트 웹툰"
        var platform: Platform = Platform.KAKAO_PAGE
        var webtoonLink: String = "www.abc"
        var thumbnailUrl: String = "www.bcd"
        var authors: String = "testtest"
        var finished: Boolean = true

        fun build(): Webtoon = Webtoon::class.java.getDeclaredConstructor().let { constructor ->
            constructor.isAccessible = true
            constructor.newInstance().also { webtoon ->
                Webtoon::class.java.getDeclaredField("webtoonName").let { field ->
                    field.isAccessible = true
                    field.set(webtoon, webtoonName)
                }
                Webtoon::class.java.getDeclaredField("platform").let { field ->
                    field.isAccessible = true
                    field.set(webtoon, platform)
                }
                Webtoon::class.java.getDeclaredField("webtoonLink").let { field ->
                    field.isAccessible = true
                    field.set(webtoon, webtoonLink)
                }
                Webtoon::class.java.getDeclaredField("thumbnailUrl").let { field ->
                    field.isAccessible = true
                    field.set(webtoon, thumbnailUrl)
                }
                Webtoon::class.java.getDeclaredField("authors").let { field ->
                    field.isAccessible = true
                    field.set(webtoon, authors)
                }
                Webtoon::class.java.getDeclaredField("finished").let { field ->
                    field.isAccessible = true
                    field.set(webtoon, finished)
                }
            }
        }
    }

    class ReviewBuilder {
        lateinit var user: WebtyUser
        lateinit var webtoon: Webtoon
        var content: String = "테스트 리뷰"
        var title: String = "테스트 리뷰 제목"
        var viewCount: Int = 0
        var isSpoiler: SpoilerStatus = SpoilerStatus.FALSE

        fun build(): Review = Review::class.java.getDeclaredConstructor().let { constructor ->
            constructor.isAccessible = true
            constructor.newInstance().also { review ->
                Review::class.java.getDeclaredField("user").let { field ->
                    field.isAccessible = true
                    field.set(review, user)
                }
                Review::class.java.getDeclaredField("webtoon").let { field ->
                    field.isAccessible = true
                    field.set(review, webtoon)
                }
                Review::class.java.getDeclaredField("content").let { field ->
                    field.isAccessible = true
                    field.set(review, content)
                }
                Review::class.java.getDeclaredField("title").let { field ->
                    field.isAccessible = true
                    field.set(review, title)
                }
                Review::class.java.getDeclaredField("viewCount").let { field ->
                    field.isAccessible = true
                    field.set(review, viewCount)
                }
                Review::class.java.getDeclaredField("isSpoiler").let { field ->
                    field.isAccessible = true
                    field.set(review, isSpoiler)
                }
            }
        }
    }

    class ReviewCommentBuilder {
        lateinit var user: WebtyUser
        lateinit var review: Review
        var content: String = "테스트 댓글"
        var parentId: Long? = null
        var depth: Int = 0
        var mentions: List<String> = emptyList()

        fun build(): ReviewComment = ReviewComment::class.java.getDeclaredConstructor().let { constructor ->
            constructor.isAccessible = true
            constructor.newInstance().also { comment ->
                ReviewComment::class.java.getDeclaredField("user").let { field ->
                    field.isAccessible = true
                    field.set(comment, user)
                }
                ReviewComment::class.java.getDeclaredField("review").let { field ->
                    field.isAccessible = true
                    field.set(comment, review)
                }
                ReviewComment::class.java.getDeclaredField("content").let { field ->
                    field.isAccessible = true
                    field.set(comment, content)
                }
                ReviewComment::class.java.getDeclaredField("parentId").let { field ->
                    field.isAccessible = true
                    field.set(comment, parentId)
                }
                ReviewComment::class.java.getDeclaredField("depth").let { field ->
                    field.isAccessible = true
                    field.set(comment, depth)
                }
                ReviewComment::class.java.getDeclaredField("mentions").let { field ->
                    field.isAccessible = true
                    field.set(comment, mentions)
                }
            }
        }
    }
}

// 스프링 부트 통합 테스트 설정
@SpringBootTest
@AutoConfigureMockMvc // MockMvc 자동 구성
@TestPropertySource( // 테스트용 프로퍼티 설정
    properties = [
        "spring.profiles.active=test", // 테스트 프로필 사용
        "spring.jpa.hibernate.ddl-auto=create-drop", // 테스트 DB 자동 생성/삭제
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE", // 인메모리 H2 DB 사용
        "spring.jpa.properties.hibernate.format_sql=true" // SQL 로그 포맷팅
    ]
)
@Transactional // 테스트 메소드 트랜잭션 처리
class ReviewCommentControllerTest @Autowired constructor(
    // 테스트에 필요한 의존성 주입
    private val reviewRepository: ReviewRepository, // 리뷰 레포지토리
    private val mockMvc: MockMvc, // HTTP 요청 테스트용 객체
    private val reviewCommentRepository: ReviewCommentRepository, // 리뷰 댓글 레포지토리
    private val webtoonRepository: WebtoonRepository, // 웹툰 레포지토리
    private val userRepository: UserRepository, // 사용자 레포지토리
    private val jwtManager: JwtManager, // JWT 토큰 관리자
    private val objectMapper: ObjectMapper, // JSON 변환용 매퍼
    private val entityManager: EntityManager // JPA 엔티티 매니저
) {
    // 테스트에서 사용할 전역 변수들
    private lateinit var testUser: WebtyUser // 테스트용 사용자
    private lateinit var testRequest: CommentRequest // 테스트용 댓글 요청
    private lateinit var testReview: Review // 테스트용 리뷰

    @BeforeEach
    fun beforeEach() {
        deleteAll()
        setupTestData()
        entityManager.flush()
        entityManager.clear()
    }

    private fun deleteAll() {
        entityManager.createQuery("DELETE FROM ReviewComment").executeUpdate()
        entityManager.createQuery("DELETE FROM Review").executeUpdate()
        entityManager.createQuery("DELETE FROM Webtoon").executeUpdate()
        entityManager.createQuery("DELETE FROM WebtyUser").executeUpdate()
        entityManager.createQuery("DELETE FROM SocialProvider").executeUpdate()
        entityManager.flush()
    }

    private fun setupTestData() {
        // 1. SocialProvider 생성 및 저장
        val socialProvider = TestEntityFactory.socialProvider {
            provider = SocialProviderType.KAKAO
            providerId = "313213231"
        }
        entityManager.persist(socialProvider)
        entityManager.flush()
        
        // 2. WebtyUser 생성 및 저장
        testUser = TestEntityFactory.webtyUser {
            nickname = "테스트유저"
            profileImage = "dasdsa"
            this.socialProvider = socialProvider
        }
        entityManager.persist(testUser)
        entityManager.flush()

        // 3. Webtoon 생성 및 저장
        val webtoon = TestEntityFactory.webtoon {
            webtoonName = "테스트 웹툰"
            platform = Platform.KAKAO_PAGE
            webtoonLink = "www.abc"
            thumbnailUrl = "www.bcd"
            authors = "testtest"
            finished = true
        }
        entityManager.persist(webtoon)
        entityManager.flush()

        // 4. Review 생성 및 저장
        testReview = TestEntityFactory.review {
            user = testUser
            this.webtoon = webtoon
            content = "테스트 리뷰"
            title = "테스트 리뷰 제목"
            viewCount = 0
            isSpoiler = SpoilerStatus.FALSE
        }
        entityManager.persist(testReview)
        entityManager.flush()

        // 5. 기본 CommentRequest 설정
        testRequest = CommentRequest(
            content = "테스트 댓글"
        )
    }

    private fun createRootComment(number: Int): ReviewComment {
        val comment = TestEntityFactory.reviewComment {
            user = testUser
            review = testReview
            content = "테스트 댓글: $number"
            parentId = null
            depth = 0
            mentions = emptyList()
        }
        entityManager.persist(comment)
        entityManager.flush()
        entityManager.clear()
        return comment
    }

    private fun createChildComment(number: Int, parentId: Long): ReviewComment {
        val comment = TestEntityFactory.reviewComment {
            user = testUser
            review = testReview
            content = "테스트 댓글: $number"
            this.parentId = parentId
            depth = 1
            mentions = emptyList()
        }
        entityManager.persist(comment)
        entityManager.flush()
        entityManager.clear()
        return comment
    }

    // 댓글 생성 API 엔드포인트 테스트
    @Test
    @DisplayName("댓글 생성 테스트")
    fun createCommentTest() {
        // Given: 테스트 데이터 준비
        val expectedContent = "테스트 댓글"
        val request = CommentRequest(
            content = expectedContent,
            parentCommentId = null, // 루트 댓글로 생성
            mentions = emptyList() // 멘션 없음
        )

        // When: API 요청 실행
        val result = mockMvc.post(getReviewCommentBasicPath(testReview.reviewId!!)) {
            header("Authorization", "Bearer ${jwtManager.createAccessToken(testUser.userId!!)}") // JWT 인증
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
            with(csrf()) // CSRF 보안 설정
        }

        // Then: 응답 검증
        result.andExpect {
            status { isOk() } // HTTP 200 상태 코드 확인
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.commentId") { exists() } // 댓글 ID 존재 확인
            jsonPath("$.content") { value(expectedContent) } // 댓글 내용 확인
            jsonPath("$.user.nickname") { value(testUser.nickname) } // 작성자 닉네임 확인
        }

        // Verify: DB 저장 데이터 검증
        val savedComment = reviewCommentRepository.findAll().first()
        assertThat(savedComment.content).isEqualTo(expectedContent)
        assertThat(savedComment.user.userId).isEqualTo(testUser.userId)
    }

    // 댓글 수정 API 엔드포인트 테스트
    @Test
    @DisplayName("댓글 수정 테스트")
    fun updateCommentTest() {
        // Given: 테스트 데이터 준비
        val testRootComment = createRootComment(1) // 루트 댓글 생성
        val childComment1 = createChildComment(1, testRootComment.commentId) // 대댓글 1 생성
        val childComment2 = createChildComment(2, testRootComment.commentId) // 대댓글 2 생성
        val expectedContent = "수정된 테스트 댓글"
        
        val updateRequest = CommentRequest(
            content = expectedContent,
            parentCommentId = null,
            mentions = emptyList()
        )

        // When: API 요청 실행
        val result = mockMvc.put("${getReviewCommentBasicPath(testReview.reviewId!!)}/${testRootComment.commentId}") {
            header("Authorization", "Bearer ${jwtManager.createAccessToken(testUser.userId!!)}") // JWT 인증
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(updateRequest)
            with(csrf())
        }

        // Then: 응답 검증
        result.andExpect {
            status { isOk() } // HTTP 200 상태 코드 확인
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.commentId") { value(testRootComment.commentId) } // 댓글 ID 확인
            jsonPath("$.content") { value(expectedContent) } // 수정된 내용 확인
            jsonPath("$.user.nickname") { value(testUser.nickname) } // 작성자 확인
            jsonPath("$.parentId") { isEmpty() } // 루트 댓글 확인
            jsonPath("$.mentions") { isEmpty() } // 멘션 없음 확인
            jsonPath("$.childComments") { isArray() } // 대댓글 목록 확인
            jsonPath("$.createdAt") { exists() } // 생성 시간 존재 확인
            jsonPath("$.modifiedAt") { exists() } // 수정 시간 존재 확인
        }
    }

    // 댓글 삭제 API 엔드포인트 테스트
    @Test
    @DisplayName("댓글 삭제 테스트")
    fun deleteCommentTest() {
        // Given: 테스트 데이터 준비
        val testRootComment = createRootComment(1) // 루트 댓글 생성
        createChildComment(1, testRootComment.commentId) // 대댓글 1 생성
        createChildComment(2, testRootComment.commentId) // 대댓글 2 생성

        // When & Then: API 요청 실행 및 검증
        mockMvc.delete("${getReviewCommentBasicPath(testReview.reviewId!!)}/${testRootComment.commentId}") {
            header("Authorization", "Bearer ${jwtManager.createAccessToken(testUser.userId!!)}") // JWT 인증
        }.andExpect {
            status { isOk() } // 삭제 성공 확인
        }
    }

    // 댓글 목록 조회 API 엔드포인트 테스트
    @Test
    @DisplayName("댓글 목록 조회 테스트")
    fun getCommentsTest() {
        // Given: 테스트 데이터 준비
        val testRootComment = createRootComment(1) // 루트 댓글 생성
        val testChildComment1 = createChildComment(1, testRootComment.commentId) // 대댓글 1 생성
        val testChildComment2 = createChildComment(2, testRootComment.commentId) // 대댓글 2 생성
        entityManager.flush()
        entityManager.clear()

        // When & Then: API 요청 실행 및 검증
        mockMvc.get(getReviewCommentBasicPath(testReview.reviewId!!)) {
            accept = MediaType.APPLICATION_JSON
            param("page", "0") // 페이지네이션 설정
            param("size", "10")
        }.andExpect {
            status { isOk() } // HTTP 200 상태 코드 확인
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.content[0].commentId") { value(testRootComment.commentId) } // 루트 댓글 ID 확인
            jsonPath("$.content[0].content") { value(testRootComment.content) } // 루트 댓글 내용 확인
            jsonPath("$.content[0].childComments") { isArray() } // 대댓글 목록 확인
            jsonPath("$.content[0].childComments.length()") { value(2) } // 대댓글 2개 확인
        }
    }

    // API 엔드포인트 경로 생성 유틸리티 메소드
    private fun getReviewCommentBasicPath(reviewId: Long): String =
        UriComponentsBuilder.fromPath("/reviews/{reviewId}/comments")
            .buildAndExpand(reviewId)
            .toUriString()
}