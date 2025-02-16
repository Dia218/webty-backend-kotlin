package org.team14.webty.webtoon.enumerate;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@AllArgsConstructor
public enum Platform {
	NAVER_WEBTOON("NAVER"),
	KAKAO_PAGE("KAKAO_PAGE");

	private final String platformName;

	public static Platform fromString(String value) {
		return Arrays.stream(values())
			.filter(platform -> platform.platformName.equalsIgnoreCase(value))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException(
				"No enum constant " + Platform.class.getCanonicalName() + "." + value));
	}
}
