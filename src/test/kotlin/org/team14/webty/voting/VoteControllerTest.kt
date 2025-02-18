package org.team14.webty.voting

import com.fasterxml.jackson.databind.ObjectMapper
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
import org.team14.webty.voting.entity.Similar
import org.team14.webty.voting.enumerate.VoteType


@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = ["spring.profiles.active=test"])
@Import(
    VotingTestDataInitializer::class
)
internal class VoteControllerTest {
    private val votePath = "/vote"

    @Autowired
    private val context: WebApplicationContext? = null

    @Autowired
    private var mockMvc: MockMvc? = null

    @Autowired
    private val votingTestDataInitializer: VotingTestDataInitializer? = null

    @Autowired
    private val jwtManager: JwtManager? = null
    private var testUser: WebtyUser? = null
    private var testSimilar: Similar? = null

    @BeforeEach
    fun beforeEach() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context!!)
            .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
            .build()

        votingTestDataInitializer!!.deleteAllData()
        testUser = votingTestDataInitializer.initTestUser()
        val testTargetWebtoon = votingTestDataInitializer.newTestTargetWebtoon(1)
        val testChoiceWebtoon = votingTestDataInitializer.newTestChoiceWebtoon(1)
        testSimilar = votingTestDataInitializer.newTestSimilar(testUser!!, testTargetWebtoon, testChoiceWebtoon)
    }

    @Test
    @DisplayName("투표 등록 테스트")
    @Throws(Exception::class)
    fun vote_test() {
        val objectMapper = ObjectMapper()

        val requestBody: MutableMap<String, String> = HashMap()
        requestBody["similarId"] = testSimilar!!.similarId.toString()
        requestBody["voteType"] = "agree"
        val jsonRequest = objectMapper.writeValueAsString(requestBody)

        mockMvc!!.perform(
            MockMvcRequestBuilders.post(votePath)
                .header("Authorization", "Bearer " + jwtManager!!.createAccessToken(testUser!!.getUserId()))
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
    }

    @Test
    @DisplayName("투표 취소 테스트")
    @Throws(Exception::class)
    fun cancel_test() {
        val testVote = votingTestDataInitializer!!.newTestVote(testUser!!, testSimilar, VoteType.AGREE)

        mockMvc!!.perform(
            MockMvcRequestBuilders.delete(votePath + "/" + testVote.voteId)
                .header("Authorization", "Bearer " + jwtManager!!.createAccessToken(testUser!!.getUserId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
    }
}