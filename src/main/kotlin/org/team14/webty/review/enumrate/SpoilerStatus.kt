package org.team14.webty.review.enumrate

import java.util.*

enum class SpoilerStatus(private val status: String) {
    TRUE("true"),
    FALSE("false");

    companion object {
        fun fromString(value: String): SpoilerStatus {
            return Arrays.stream(entries.toTypedArray())
                .filter { status: SpoilerStatus -> status.status.equals(value, ignoreCase = true) }
                .findFirst().orElseThrow {
                    IllegalArgumentException(
                        "No enum constant " + SpoilerStatus::class.java.canonicalName + "." + value
                    )
                }
        }
    }
}
