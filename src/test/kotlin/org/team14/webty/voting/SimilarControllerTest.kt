package org.team14.webty.voting

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.collection.IsCollectionWithSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.team14.webty.security.token.JwtManager
import org.team14.webty.user.entity.WebtyUser
import org.team14.webty.webtoon.entity.Webtoon

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = ["spring.profiles.active=test"])
@Import(
    VotingTestDataInitializer::class
)
internal class SimilarControllerTest {
    private val similarPath = "/similar"

    @Autowired
    private val context: WebApplicationContext? = null

    @Autowired
    private var mockMvc: MockMvc? = null

    @Autowired
    private val votingTestDataInitializer: VotingTestDataInitializer? = null

    @Autowired
    private val jwtManager: JwtManager? = null
    private var testUser: WebtyUser? = null
    private var testTargetWebtoon: Webtoon? = null
    private var testChoiceWebtoon: Webtoon? = null

    @BeforeEach
    fun beforeEach() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context!!)
            .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
            .build()

        votingTestDataInitializer!!.deleteAllData()
        testUser = votingTestDataInitializer.initTestUser()
        testTargetWebtoon = votingTestDataInitializer.newTestTargetWebtoon(1)
        testChoiceWebtoon = votingTestDataInitializer.newTestChoiceWebtoon(1)
    }

    @Test
    @DisplayName("유사 등록 테스트")
    @Throws(Exception::class)
    fun createSimilar_test() {
        val testTargetWebtoonId = testTargetWebtoon!!.webtoonId
        val testChoiceWebtoonId = testChoiceWebtoon!!.webtoonId

        val objectMapper = ObjectMapper()

        val requestBody: MutableMap<String, Long> = HashMap()
        requestBody["targetWebtoonId"] = testTargetWebtoonId!!
        requestBody["choiceWebtoonId"] = testChoiceWebtoonId!!
        val jsonRequest = objectMapper.writeValueAsString(requestBody)

        mockMvc!!.perform(
            MockMvcRequestBuilders.post(similarPath)
                .header("Authorization", "Bearer " + jwtManager!!.createAccessToken(testUser!!.userId!!))
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.similarThumbnailUrl").value(testChoiceWebtoon!!.thumbnailUrl))
            .andExpect(MockMvcResultMatchers.jsonPath("$.similarWebtoonId").value(testChoiceWebtoon!!.webtoonId))
    }

    @Test
    @DisplayName("유사 삭제 테스트")
    @Throws(Exception::class)
    fun deleteSimilar_test() {
        val testSimilar = votingTestDataInitializer!!.newTestSimilar(
            testUser!!, testTargetWebtoon!!,
            testChoiceWebtoon!!
        )

        mockMvc!!.perform(
            MockMvcRequestBuilders.delete(similarPath + "/" + testSimilar.similarId)
                .header("Authorization", "Bearer " + jwtManager!!.createAccessToken(testUser!!.userId!!))
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
    }

    @Test
    @DisplayName("유사 목록 조회 테스트")
    @Throws(java.lang.Exception::class)
    fun similarList_test() {
        val testChoiceWebtoon2 = votingTestDataInitializer!!.newTestChoiceWebtoon(2)
        votingTestDataInitializer.newTestSimilar(testUser!!, testTargetWebtoon!!, testChoiceWebtoon!!)
        votingTestDataInitializer.newTestSimilar(testUser!!, testTargetWebtoon!!, testChoiceWebtoon2)

        mockMvc!!.perform(
            MockMvcRequestBuilders.get(similarPath)
                .param("targetWebtoonId", testTargetWebtoon!!.webtoonId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content", IsCollectionWithSize.hasSize<Any>(2)))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].similarWebtoonId").value(testChoiceWebtoon!!.webtoonId)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].similarWebtoonId").value(testChoiceWebtoon2.webtoonId)
            )
    }
}