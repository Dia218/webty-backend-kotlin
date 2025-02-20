package org.team14.webty.reviewComment.controller

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
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
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.assertj.core.api.Assertions.assertThat
import jakarta.persistence.EntityManager

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

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
    properties = [
        "spring.profiles.active=test",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.jpa.properties.hibernate.format_sql=true"
    ]
)
@Transactional
class ReviewCommentControllerTest @Autowired constructor(
    private val reviewRepository: ReviewRepository,
    private val mockMvc: MockMvc,
    private val reviewCommentRepository: ReviewCommentRepository,
    private val webtoonRepository: WebtoonRepository,
    private val userRepository: UserRepository,
    private val jwtManager: JwtManager,
    private val objectMapper: ObjectMapper,
    private val entityManager: EntityManager
) {
    private lateinit var testUser: WebtyUser
    private lateinit var testRequest: CommentRequest
    private lateinit var testReview: Review

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

    @Test
    @DisplayName("댓글 생성 테스트")
    fun createCommentTest() {
        // Given
        val expectedContent = "테스트 댓글"
        val request = CommentRequest(content = expectedContent)

        // When
        val result = mockMvc.post(getReviewCommentBasicPath(testReview.reviewId!!)) {
            header("Authorization", "Bearer ${jwtManager.createAccessToken(testUser.userId!!)}")
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
            with(csrf())
        }

        // Then
        result.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.commentId") { exists() }
            jsonPath("$.content") { value(expectedContent) }
            jsonPath("$.user.nickname") { value(testUser.nickname) }
            jsonPath("$.parentId") { isEmpty() }
            jsonPath("$.mentions") { isArray() }
            jsonPath("$.childComments") { isArray() }
            jsonPath("$.createdAt") { exists() }
            jsonPath("$.modifiedAt") { exists() }
        }

        // Verify
        val savedComment = reviewCommentRepository.findAll().first()
        assertThat(savedComment.content).isEqualTo(expectedContent)
        assertThat(savedComment.user.userId).isEqualTo(testUser.userId)
    }

    @Test
    @DisplayName("댓글 수정 테스트")
    fun updateCommentTest() {
        val testRootComment = createRootComment(1)
        val childComment1 = createChildComment(1, testRootComment.commentId)
        val childComment2 = createChildComment(2, testRootComment.commentId)
        val expectedContent = "수정된 테스트 댓글"
        
        val updateRequest = CommentRequest(
            content = expectedContent,
            parentCommentId = 0,
            mentions = emptyList()
        )

        val result = mockMvc.put("${getReviewCommentBasicPath(testReview.reviewId!!)}/${testRootComment.commentId}") {
            header("Authorization", "Bearer ${jwtManager.createAccessToken(testUser.userId!!)}")
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(updateRequest)
            with(csrf())
        }

        result.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.commentId") { value(testRootComment.commentId) }
            jsonPath("$.content") { value(expectedContent) }
            jsonPath("$.user.nickname") { value(testUser.nickname) }
            jsonPath("$.parentId") { isEmpty() }
            jsonPath("$.mentions") { isEmpty() }
            jsonPath("$.childComments") { isArray() }
            jsonPath("$.createdAt") { exists() }
            jsonPath("$.modifiedAt") { exists() }
        }
    }

    @Test
    @DisplayName("댓글 삭제 테스트")
    fun deleteCommentTest() {
        val testRootComment = createRootComment(1)
        createChildComment(1, testRootComment.commentId)
        createChildComment(2, testRootComment.commentId)

        mockMvc.delete("${getReviewCommentBasicPath(testReview.reviewId!!)}/${testRootComment.commentId}") {
            header("Authorization", "Bearer ${jwtManager.createAccessToken(testUser.userId!!)}")
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    @DisplayName("댓글 목록 조회 테스트")
    fun getCommentsTest() {
        val testRootComment = createRootComment(1)
        val testChildComment1 = createChildComment(1, testRootComment.commentId)
        val testChildComment2 = createChildComment(2, testRootComment.commentId)

        mockMvc.get(getReviewCommentBasicPath(testReview.reviewId!!)) {
            header("Authorization", "Bearer ${jwtManager.createAccessToken(testUser.userId!!)}")
            accept = MediaType.APPLICATION_JSON
            param("page", "0")
            param("size", "10")
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.content") { isArray() }
            jsonPath("$.content[0].commentId") { value(testRootComment.commentId) }
            jsonPath("$.content[0].content") { value(testRootComment.content) }
            jsonPath("$.content[0].user.nickname") { value(testUser.nickname) }
            jsonPath("$.content[0].childComments[0].commentId") { value(testChildComment2.commentId) }
            jsonPath("$.content[0].childComments[0].content") { value(testChildComment2.content) }
            jsonPath("$.content[0].childComments[0].parentId") { value(testChildComment2.parentId) }
            jsonPath("$.content[0].childComments[1].commentId") { value(testChildComment1.commentId) }
            jsonPath("$.content[0].childComments[1].content") { value(testChildComment1.content) }
            jsonPath("$.content[0].childComments[1].parentId") { value(testChildComment1.parentId) }
            jsonPath("$.content[0].childComments[0].user.nickname") { value(testUser.nickname) }
            jsonPath("$.number") { value(0) }
            jsonPath("$.totalPages") { isNumber() }
            jsonPath("$.totalElements") { isNumber() }
            jsonPath("$.last") { isBoolean() }
            jsonPath("$.first") { isBoolean() }
            jsonPath("$.empty") { isBoolean() }
            jsonPath("$.content[0].createdAt") { exists() }
            jsonPath("$.content[0].modifiedAt") { exists() }
            jsonPath("$.content[0].childComments[0].createdAt") { exists() }
            jsonPath("$.content[0].childComments[0].modifiedAt") { exists() }
        }
    }

    private fun getReviewCommentBasicPath(reviewId: Long): String =
        UriComponentsBuilder.fromPath("/reviews/{reviewId}/comments")
            .buildAndExpand(reviewId)
            .toUriString()
}