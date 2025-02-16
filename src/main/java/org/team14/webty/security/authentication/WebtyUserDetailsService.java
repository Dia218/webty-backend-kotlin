package org.team14.webty.security.authentication;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.team14.webty.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WebtyUserDetailsService implements UserDetailsService {

	private final UserService userService;

	@Override
	public WebtyUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return new WebtyUserDetails(userService.findByNickName(username));
	}

	public WebtyUserDetails loadUserByUserId(Long userId) {
		return loadUserByUsername(userService.findNickNameByUserId(userId));
	}
}