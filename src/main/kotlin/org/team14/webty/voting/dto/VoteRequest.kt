package org.team14.webty.voting.dto

data class VoteRequest(
    val similarId: Long,
    val voteType: String,
    val page: Int?,
    val size: Int?
) {
    val validatedPage: Int = page ?: 0
    val validatedSize: Int = size?.takeIf { it >= 1 } ?: 10
}
