package org.team14.webty.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NicknameRequest {
	@NotBlank(message = "닉네임은 필수항목입니다.")
	private String nickname;
}
