package org.team14.webty.webtoon.api

import com.fasterxml.jackson.annotation.JsonProperty

data class WebtoonPageApiResponse(

        @JsonProperty("webtoons")
        val webtoonApiResponses: List<WebtoonApiResponse> = emptyList(),

        @JsonProperty("total")
        val total: Int = 0,

        @JsonProperty("isLastPage")
        val isLastPage: Boolean = false
)
