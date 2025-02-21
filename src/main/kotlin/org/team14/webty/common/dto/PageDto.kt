package org.team14.webty.common.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class PageDto<T>(
    val content: List<T> = emptyList(),
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
    @get:JsonProperty("isLast")
    val isLast: Boolean = false
)