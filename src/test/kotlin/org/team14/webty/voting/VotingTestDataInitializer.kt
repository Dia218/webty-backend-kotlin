package org.team14.webty.voting

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestComponent
import org.team14.webty.user.entity.SocialProvider
import org.team14.webty.user.entity.WebtyUser
import org.team14.webty.user.enums.SocialProviderType
import org.team14.webty.user.repository.UserRepository
import org.team14.webty.voting.entity.Similar
import org.team14.webty.voting.entity.Vote
import org.team14.webty.voting.enums.VoteType
import org.team14.webty.voting.repository.SimilarRepository
import org.team14.webty.voting.repository.VoteRepository
import org.team14.webty.webtoon.entity.Webtoon
import org.team14.webty.webtoon.enumerate.Platform
import org.team14.webty.webtoon.repository.WebtoonRepository


@TestComponent
class VotingTestDataInitializer {
    @Autowired
    private val userRepository: UserRepository? = null

    @Autowired
    private val webtoonRepository: WebtoonRepository? = null

    @Autowired
    private val similarRepository: SimilarRepository? = null

    @Autowired
    private val voteRepository: VoteRepository? = null

    fun deleteAllData() {
        voteRepository?.deleteAll()
        similarRepository?.deleteAll()
        webtoonRepository?.deleteAll()
        userRepository?.deleteAll()
    }

    fun initTestUser(): WebtyUser {
        return userRepository!!.save(
                WebtyUser(
                        nickname = "테스트유저",
                        profileImage = "testUserProfileImg",
                        socialProvider = SocialProvider(
                                provider = SocialProviderType.KAKAO,
                                providerId = "123456789"
                        )
                )
        )
    }

    fun newTestTargetWebtoon(number: Int): Webtoon {
        return webtoonRepository!!.save(
            Webtoon(
                webtoonName = "테스트 투표 대상 웹툰$number",
                platform = Platform.KAKAO_PAGE,
                webtoonLink = "www.testTargetWebtoon$number",
                thumbnailUrl = "testTargetWebtoon.jpg$number",
                authors = "testTargetWebtoonAuthor$number",
                finished = true
            )
        )
    }

    fun newTestChoiceWebtoon(number: Int): Webtoon {
        return webtoonRepository!!.save(
            Webtoon(
                webtoonName = "테스트 선택 대상 웹툰$number",
                platform = Platform.KAKAO_PAGE,
                webtoonLink = "www.testChoiceWebtoon$number",
                thumbnailUrl = "testChoiceWebtoon.jpg$number",
                authors = "testChoiceWebtoonAuthor$number",
                finished = true
            )
        )
    }

    fun newTestSimilar(testUser: WebtyUser, testTargetWebtoon: Webtoon, testChoiceWebtoon: Webtoon): Similar {
        return similarRepository!!.save(
            Similar(
                similarWebtoonId = testChoiceWebtoon.webtoonId!!,
                similarResult = 0L,
                userId = testUser.userId!!,
                targetWebtoon = testTargetWebtoon
            )
        )
    }

    fun newTestVote(testUser: WebtyUser, testSimilar: Similar, voteType: VoteType): Vote {
        return voteRepository!!.save(
            Vote(
                userId = testUser.userId!!,
                similar = testSimilar,
                voteType = voteType
            )
        )
    }
}
