package org.team14.webty.webtoon.api

import com.fasterxml.jackson.annotation.JsonProperty

data class WebtoonApiResponse(

        @JsonProperty("title")
        val title: String,
        @JsonProperty("provider")
        val provider: String,
        @JsonProperty("url")
        val url: String,
        @JsonProperty("thumbnail")
        val thumbnails: String,
        @JsonProperty("isEnd")
        val isEnd: Boolean,
        @JsonProperty("authors")
        val authors: List<String>

)
