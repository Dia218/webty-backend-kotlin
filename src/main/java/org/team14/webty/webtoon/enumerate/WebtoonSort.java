package org.team14.webty.webtoon.enumerate;

import java.util.Arrays;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WebtoonSort {
	WEBTOON_NAME("webtoonName"),
	PLATFORM("platform"),
	AUTHORS("authors"),
	FINISHED("finished");

	private final String field;

	public static Optional<WebtoonSort> fromString(String value) {
		return Arrays.stream(WebtoonSort.values())
			.filter(sort -> sort.name().equalsIgnoreCase(value))
			.findFirst();
	}
}