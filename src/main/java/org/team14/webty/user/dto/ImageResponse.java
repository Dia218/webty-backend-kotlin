package org.team14.webty.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageResponse {
	private String message;

	public ImageResponse(String message) {
		this.message = message;
	}
}
