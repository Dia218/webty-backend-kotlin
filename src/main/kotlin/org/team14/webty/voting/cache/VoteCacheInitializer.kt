package org.team14.webty.voting.cache

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.team14.webty.voting.repository.VoteRepository

@Component
class VoteCacheInitializer(
    private val voteRepository: VoteRepository,
    private val voteCacheService: VoteCacheService
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        val votes = voteRepository.findAll()

        votes.forEach { vote ->
            voteCacheService.setUserVote(vote.similar.similarId!!, vote.userId)
        }

        votes.groupBy { it.similar.similarId!! to it.voteType }
            .forEach { (key, voteList) ->
                val (similarId, voteType) = key
                voteCacheService.setVoteCount(similarId, voteType, voteList.size.toLong())
            }
    }
}