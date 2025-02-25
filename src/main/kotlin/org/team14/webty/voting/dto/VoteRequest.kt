package org.team14.webty.voting.dto

data class VoteRequest(
    val similarId: Long,
    val voteType: String,
    val page: Int = 0,
    val size: Int = 10
)
