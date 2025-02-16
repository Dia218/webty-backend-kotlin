package org.team14.webty.voting.enumerate;

import java.util.Arrays;

import org.team14.webty.common.exception.BusinessException;
import org.team14.webty.common.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VoteType {
	AGREE("agree"),
	DISAGREE("disagree");

	private final String type;

	public static VoteType fromString(String value) {
		return Arrays.stream(values())
			.filter(status -> status.type.equalsIgnoreCase(value))
			.findFirst().orElseThrow(() -> new BusinessException(ErrorCode.RECOMMEND_TYPE_ERROR));
	}
}
