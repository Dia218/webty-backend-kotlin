package org.team14.webty.voting.dto

data class SimilarRequest(
    val targetWebtoonId: Long,
    val choiceWebtoonId: Long
)