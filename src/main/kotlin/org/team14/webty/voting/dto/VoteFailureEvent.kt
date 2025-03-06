package org.team14.webty.voting.dto

import org.team14.webty.voting.enums.VoteFailureType

data class VoteFailureEvent(
    val similarId: Long,
    val userId: Long,
    val voteFailureType: VoteFailureType
)
