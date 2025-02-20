package org.team14.webty.webtoon.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class FavoriteDto (

    @JsonProperty("webtoonId")
    val webtoonId: Long?

)