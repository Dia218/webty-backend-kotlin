package org.team14.webty.voting.dto

data class VoteSuccessEvent(
    val similarId: Long,
    val userId: Long
)
