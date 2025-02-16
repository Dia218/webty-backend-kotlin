package org.team14.webty.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NicknameResponse {
	private String message;

	public NicknameResponse(String message) {
		this.message = message;
	}
}
