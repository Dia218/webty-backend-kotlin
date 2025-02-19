package org.team14.webty.voting.mapper

import org.team14.webty.user.entity.WebtyUser
import org.team14.webty.voting.entity.Similar
import org.team14.webty.voting.entity.Vote
import org.team14.webty.voting.enums.VoteType

object VoteMapper {
    fun toEntity(webtyUser: WebtyUser, similar: Similar, type: String): Vote {
        return Vote(
            userId = webtyUser.userId,
            similar = similar,
            voteType = VoteType.fromString(type)
        )
    }
}
