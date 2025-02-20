package org.team14.webty.voting.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.team14.webty.voting.entity.Similar
import org.team14.webty.voting.entity.Vote
import org.team14.webty.voting.enums.VoteType
import java.util.*

@Repository
interface VoteRepository : JpaRepository<Vote, Long> {
    
    fun findBySimilarAndUserId(similar: Similar, userId: Long): Optional<Vote>
    
    fun existsByUserIdAndSimilar(userId: Long, similar: Similar): Boolean
    
    fun deleteBySimilarAndUserId(similar: Similar, userId: Long): Long
    
    fun findAllBySimilar(similar: Similar): List<Vote>
    
    fun countBySimilarAndVoteType(similar: Similar, voteType: VoteType): Long
}
