package org.team14.webty.voting.enums

import lombok.AllArgsConstructor
import lombok.Getter
import org.team14.webty.common.exception.BusinessException
import org.team14.webty.common.exception.ErrorCode
import java.util.*


@Getter
@AllArgsConstructor
enum class VoteType(
        private val type: String
) {
    AGREE("agree"),
    DISAGREE("disagree");

    companion object {
        fun fromString(value: String): VoteType {
            return Arrays.stream(entries.toTypedArray())
                    .filter { status: VoteType ->
                        status.type.equals(
                                value,
                                ignoreCase = true
                        )
                    }
                    .findFirst().orElseThrow {
                        BusinessException(
                                ErrorCode.RECOMMEND_TYPE_ERROR
                        )
                    }
        }
    }
}
