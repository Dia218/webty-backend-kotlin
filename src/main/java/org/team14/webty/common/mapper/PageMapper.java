package org.team14.webty.common.mapper;

import org.springframework.data.domain.Page;
import org.team14.webty.common.dto.PageDto;

public class PageMapper {
	public static <T> PageDto<T> toPageDto(Page<T> page) {
		return PageDto.<T>builder()
			.content(page.getContent())
			.currentPage(page.getNumber())
			.totalPages(page.getTotalPages())
			.totalElements(page.getTotalElements())
			.hasNext(page.hasNext())
			.hasPrevious(page.hasPrevious())
			.isLast(page.isLast())
			.build();
	}
}
