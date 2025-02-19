package org.team14.webty.security.token

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.core.Authentication
import org.springframework.test.context.ActiveProfiles
import org.team14.webty.user.entity.WebtyUser
import org.team14.webty.user.repository.UserRepository
import java.util.*

@SpringBootTest
@ActiveProfiles("test") // application-test.yml 사용
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class JwtManagerTest {

    private val nickName = "testUser"

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var jwtManager: JwtManager

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, String> // 실제 Redis 활용

    private var userId: Long? = null
    private lateinit var accessToken: String
    private lateinit var refreshToken: String

    @BeforeEach
    fun setUp() {
        // 1. 테스트 유저 생성 및 저장
        val testUser = WebtyUser(null, nickName, null, null)
        userId = userRepository.save(testUser).userId

        // 2. JWT 토큰 생성
        accessToken = jwtManager.createAccessToken(userId!!)
        refreshToken = jwtManager.createRefreshToken(userId!!)
    }

    @AfterEach
    fun tearDown() {
        userRepository.deleteAll()
        redisTemplate.delete(refreshToken) // Redis에서 refreshToken 삭제
    }

    @Test
    fun `should create access token`() {
        assertThat(accessToken).isNotNull()
        assertThat(jwtManager.validate(accessToken)).isTrue()
    }

    @Test
    fun `should create refresh token and store in Redis`() {
        assertThat(refreshToken).isNotNull()
        assertThat(jwtManager.validate(refreshToken)).isTrue()

        // Redis에 refreshToken이 저장되었는지 검증
        val storedUserId = redisTemplate.opsForValue().get(refreshToken)
        assertThat(storedUserId).isEqualTo(userId.toString())
    }

    @Test
    fun `should return valid expiration time`() {
        val expirationTime = jwtManager.getExpirationTime(accessToken)
        assertThat(expirationTime).isGreaterThan(System.currentTimeMillis())
    }

    @Test
    fun `should validate active token`() {
        assertThat(jwtManager.validate(accessToken)).isTrue()
    }

    @Test
    fun `should return false for expired token`() {
        // 만료된 토큰 생성 (0ms 후 만료)
        val expiredToken = Jwts.builder()
            .claim("userId", userId)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() - 1000)) // 이미 만료됨
            .signWith(Keys.hmacShaKeyFor("testtesttesttesttesttesttesttesttesttest".toByteArray()))
            .compact()

        assertThat(jwtManager.validate(expiredToken)).isFalse()
    }

    @Test
    fun `should recreate tokens and store new refresh token in Redis`() {
        val newTokens = jwtManager.recreateTokens(refreshToken)
        assertThat(newTokens).hasSize(2)
        assertThat(jwtManager.validate(newTokens[0])).isTrue()
        assertThat(jwtManager.validate(newTokens[1])).isTrue()

        // Redis에 새로운 refreshToken이 저장되었는지 확인
        val storedUserId = redisTemplate.opsForValue().get(newTokens[1])
        assertThat(storedUserId).isEqualTo(userId.toString())
    }

    @Test
    fun `should extract user id from token`() {
        val extractedUserId = jwtManager.getUserIdByToken(accessToken)
        assertThat(extractedUserId).isEqualTo(userId)
    }

    @Test
    fun `should get authentication from token`() {
        val authentication: Authentication = jwtManager.getAuthentication(accessToken)
        assertThat(authentication).isNotNull
        assertThat(authentication.name).isEqualTo(nickName)
        assertThat(authentication.authorities).isNotEmpty
    }
}
