package org.team14.webty.voting.dto

data class VoteRequest(
    val similarId: Long,
    val voteType: String
)
