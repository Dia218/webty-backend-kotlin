package org.team14.webty.recommend.enums

import org.team14.webty.common.exception.BusinessException
import org.team14.webty.common.exception.ErrorCode

enum class LikeType(val type: String) {
    LIKE("like"),
    HATE("hate");

    companion object {
        fun fromString(value: String): LikeType {
            return entries.firstOrNull { it.type.equals(value, ignoreCase = true) }
                ?: throw BusinessException(ErrorCode.RECOMMEND_TYPE_ERROR)
        }
    }
}