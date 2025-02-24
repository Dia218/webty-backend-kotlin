package org.team14.webty.voting.enums

import org.team14.webty.common.exception.BusinessException
import org.team14.webty.common.exception.ErrorCode

enum class VoteType(
    private val type: String
) {
    AGREE("agree"),
    DISAGREE("disagree");

    companion object {
        fun fromString(value: String): VoteType {
            return entries.firstOrNull { it.type.equals(value, ignoreCase = true) }
                ?: throw BusinessException(ErrorCode.VOTE_TYPE_ERROR)
        }
    }
}
