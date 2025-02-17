package org.team14.webty.common.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageDto<T> {
	private List<T> content;
	private int currentPage;
	private int totalPages;
	private long totalElements;
	private boolean hasNext;
	private boolean hasPrevious;
	@JsonProperty("isLast")
	private boolean isLast;
}
