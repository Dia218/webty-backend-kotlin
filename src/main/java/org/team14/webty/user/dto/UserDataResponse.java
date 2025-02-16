package org.team14.webty.user.dto;

import org.team14.webty.user.entity.WebtyUser;

import lombok.Getter;

@Getter
public class UserDataResponse { // UserInfoResponse가 스프링에 이미 있어서 UserDataResponse 사용
	private final Long id;
	private final String nickname;
	private final String profileImage;

	public UserDataResponse(WebtyUser webtyUser) {
		this.id = webtyUser.getUserId();
		this.nickname = webtyUser.getNickname();
		this.profileImage = webtyUser.getProfileImage();
	}
}
