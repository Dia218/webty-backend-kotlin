package org.team14.webty.security.authentication;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.team14.webty.user.entity.WebtyUser;

import lombok.Getter;

@Getter
public class WebtyUserDetails implements UserDetails {

	public final WebtyUser webtyUser;
	final String USER_ROLE = "ROLE_USER";

	public WebtyUserDetails(WebtyUser webtyUser) {
		this.webtyUser = webtyUser;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return AuthorityUtils.createAuthorityList(USER_ROLE);
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getUsername() {
		return webtyUser.getNickname();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;  // 계정이 만료되지 않았다는 조건
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;  // 계정이 잠기지 않았다는 조건
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;  // 자격 증명이 만료되지 않았다는 조건
	}

	@Override
	public boolean isEnabled() {
		return true;  // 계정이 활성화되었는지 여부
	}

}
