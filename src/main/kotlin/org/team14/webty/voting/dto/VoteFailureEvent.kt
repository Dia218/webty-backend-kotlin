package org.team14.webty.voting.dto

data class VoteFailureEvent(
    val similarId: Long,
    val userId: Long
)
