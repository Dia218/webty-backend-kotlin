package org.team14.webty.webtoon

import org.junit.jupiter.api.AfterEach
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
import org.team14.webty.security.token.JwtManager
import org.team14.webty.user.entity.SocialProvider
import org.team14.webty.user.entity.WebtyUser
import org.team14.webty.user.enums.SocialProviderType
import org.team14.webty.user.repository.UserRepository
import org.team14.webty.webtoon.entity.Webtoon
import org.team14.webty.webtoon.enumerate.Platform
import org.team14.webty.webtoon.repository.WebtoonRepository


@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = ["spring.profiles.active=test"])
class WebtoonControllerTest {
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Autowired
    private lateinit var userRepository: UserRepository
    
    @Autowired
    private lateinit var webtoonRepository: WebtoonRepository
    
    @Autowired
    private val jwtManager: JwtManager? = null
    private var testUser: WebtyUser? = null
    private var testWebtoon: Webtoon? = null
    
    
    @BeforeEach
    fun beforeEach() {
        webtoonRepository.deleteAll()
        userRepository.deleteAll()
        
        testUser = userRepository.save(
            WebtyUser(
                nickname = "테스트유저",
                profileImage = "dasdsa",
                socialProvider = SocialProvider(
                    provider = SocialProviderType.KAKAO,
                    providerId = "313213231"
                )
            )
        )
        
        testWebtoon = webtoonRepository.save(
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
        webtoonRepository.deleteAll()
        userRepository.deleteAll()
    }
    
    @Test
    @DisplayName("단일 웹툰 조회 테스트")
    fun t1() {
        val webtoonId = testWebtoon!!.webtoonId
        val accessToken = jwtManager!!.createAccessToken(testUser!!.userId!!)
        
        mockMvc.perform(
            MockMvcRequestBuilders.get("/webtoons/$webtoonId")
                .header("Authorization", "Bearer $accessToken")
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.webtoonName").value("테스트 웹툰"))
    }
    
    @Test
    @DisplayName("웹툰 전체조회 테스트")
    fun t2() {
        val page = 0
        val size = 10
        val accessToken = jwtManager!!.createAccessToken(testUser!!.userId!!)
        
        mockMvc.perform(
            MockMvcRequestBuilders.get("/webtoons")
                .header("Authorization", "Bearer $accessToken")
                .param("page", page.toString())
                .param("size", size.toString())
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(1))
    }
    
    @Test
    @DisplayName("웹툰 검색 테스트 - 작가이름으로 조회")
    fun t3() {
        val page = 0
        val size = 10
        val authors = "Author1"
        val accessToken = jwtManager!!.createAccessToken(testUser!!.userId!!)
        
        mockMvc.perform(
            MockMvcRequestBuilders.get("/webtoons")
                .header("Authorization", "Bearer $accessToken")
                .param("authors", authors)
                .param("page", page.toString())
                .param("size", size.toString())
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(1))
    }
}
