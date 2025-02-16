package org.team14.webty.review.enumrate;

import java.util.Arrays;

import lombok.Getter;

@Getter
public enum SpoilerStatus {
	TRUE("true"),
	FALSE("false");

	private final String status;

	SpoilerStatus(String status) {
		this.status = status;
	}

	public static SpoilerStatus fromString(String value) {
		return Arrays.stream(values())
			.filter(status -> status.status.equalsIgnoreCase(value))
			.findFirst().orElseThrow(() -> new IllegalArgumentException(
				"No enum constant " + SpoilerStatus.class.getCanonicalName() + "." + value));
	}
}
