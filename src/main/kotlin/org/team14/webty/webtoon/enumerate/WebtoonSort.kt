package org.team14.webty.webtoon.enumerate

enum class WebtoonSort(val field: String) {
    WEBTOON_NAME("webtoonName"),
    PLATFORM("platform"),
    AUTHORS("authors"),
    FINISHED("finished");

    companion object {
        fun fromString(value: String): WebtoonSort? {
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }
    }
}