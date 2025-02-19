package org.team14.webty.voting.dto

import jakarta.validation.constraints.NotNull

@JvmRecord
data class SimilarRequest(val targetWebtoonId: @NotNull Long, val choiceWebtoonId: @NotNull Long)