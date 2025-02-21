package org.team14.webty.common.mapper

import org.springframework.data.domain.Page
import org.team14.webty.common.dto.PageDto

object PageMapper {
    fun <T> toPageDto(page: Page<T>): PageDto<T> {
        return PageDto(
            content = page.content,
            currentPage = page.number,
            totalPages = page.totalPages,
            totalElements = page.totalElements,
            hasNext = page.hasNext(),
            hasPrevious = page.hasPrevious(),
            isLast = page.isLast
        )
    }
}