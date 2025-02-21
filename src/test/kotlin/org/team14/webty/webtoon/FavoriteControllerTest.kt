package org.team14.webty.webtoon


import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import org.team14.webty.security.token.JwtManager
import org.team14.webty.user.entity.SocialProvider
import org.team14.webty.user.entity.WebtyUser
import org.team14.webty.user.enums.SocialProviderType
import org.team14.webty.user.repository.UserRepository
import org.team14.webty.webtoon.entity.Webtoon
import org.team14.webty.webtoon.enumerate.Platform
import org.team14.webty.webtoon.mapper.FavoriteMapper
import org.team14.webty.webtoon.repository.FavoriteRepository
import org.team14.webty.webtoon.repository.WebtoonRepository

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = ["spring.profiles.active=test"])
class FavoriteControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc
    @Autowired
    lateinit var userRepository: UserRepository
    @Autowired
    lateinit var webtoonRepository: WebtoonRepository
    @Autowired
    lateinit var favoriteRepository: FavoriteRepository
    @Autowired
    private val jwtManager: JwtManager? = null
    private var testUser: WebtyUser? = null
    private var testWebtoon: Webtoon? = null
    private var testSocialProvider:SocialProviderType? = null

    @BeforeEach
    fun beforeEach() {
        favoriteRepository.deleteAll() // 참조 무결성 때문에 favorite DB 먼저 삭제
        webtoonRepository.deleteAll()
        userRepository.deleteAll()

        testUser = userRepository?.save(
            WebtyUser(
                nickname="테스트유저",
                profileImage="dasdsa"
            )
        )

        testWebtoon = webtoonRepository!!.save(
            Webtoon(
                webtoonName = "테스트 웹툰",
                platform = Platform.KAKAO_PAGE,
                webtoonLink = "www.abc",
                thumbnailUrl = "www.bcd",
                authors = "Author1",
                finished = true
            )
        )

    }

    @AfterEach
    fun afterEach() {
        favoriteRepository.deleteAll()
        webtoonRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    @DisplayName("웹툰 추천 테스트")
    @Transactional
    fun t1() {
        val webtoonId = testWebtoon!!.webtoonId
        val accessToken = jwtManager!!.createAccessToken(testUser!!.userId!!)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/favorite/$webtoonId")
                .header("Authorization", "Bearer $accessToken")
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
    }

    @Test
    @DisplayName("추천웹툰 취소 테스트")
    @Transactional
    fun t2() {
        val webtoonId = testWebtoon!!.webtoonId
        val accessToken = jwtManager!!.createAccessToken(testUser!!.userId!!)

        favoriteRepository!!.save(FavoriteMapper.toEntity(testUser!!, testWebtoon!!))

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/favorite/$webtoonId")
                .header("Authorization", "Bearer $accessToken")
        )
            .andExpect(MockMvcResultMatchers.status().isOk()) // 1. 상태코드 200ok인지 확인

        assertTrue(favoriteRepository!!.findByWebtyUserAndWebtoon(testUser!!, testWebtoon!!).isEmpty)
        // 2. db에서 삭제됐는지 확인
    }

    @Test
    @DisplayName("유저의 추천웹툰 목록 테스트")
    @Transactional
    fun t3() {
        val accessToken = jwtManager!!.createAccessToken(testUser!!.userId!!)

        favoriteRepository!!.save(FavoriteMapper.toEntity(testUser!!, testWebtoon!!))

        mockMvc.perform(
            MockMvcRequestBuilders.get("/favorite/list")
                .header("Authorization", "Bearer $accessToken")
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].webtoonName").value("테스트 웹툰"))
    }

    @Test
    @DisplayName("유저가 추천웹툰으로 등록했는지 여부 테스트")
    @Transactional
    fun t4() {
        val webtoonId = testWebtoon!!.webtoonId
        val accessToken = jwtManager!!.createAccessToken(testUser!!.userId!!)

        favoriteRepository.save(FavoriteMapper.toEntity(testUser!!, testWebtoon!!))

        mockMvc.perform(
            MockMvcRequestBuilders.get("/favorite/$webtoonId")
                .header("Authorization", "Bearer $accessToken")
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$").value("true"))
    }
}