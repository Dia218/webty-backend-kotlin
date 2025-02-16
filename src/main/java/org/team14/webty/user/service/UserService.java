package org.team14.webty.user.service;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.team14.webty.common.exception.BusinessException;
import org.team14.webty.common.exception.ErrorCode;
import org.team14.webty.common.util.FileStorageUtil;
import org.team14.webty.security.authentication.AuthWebtyUserProvider;
import org.team14.webty.security.authentication.WebtyUserDetails;
import org.team14.webty.user.entity.SocialProvider;
import org.team14.webty.user.entity.WebtyUser;
import org.team14.webty.user.enumerate.SocialProviderType;
import org.team14.webty.user.repository.SocialProviderRepository;
import org.team14.webty.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	@Value("${default-profile-image}")
	private String defaultProfileImagePath;
	private static final String DEFAULT_NICKNAME = "웹티사랑꾼 %s";

	private final UserRepository userRepository;
	private final SocialProviderRepository socialProviderRepository;
	private final AuthWebtyUserProvider authWebtyUserProvider;
	private final FileStorageUtil fileStorageUtil;

	@Transactional(readOnly = true)
	public Optional<Long> existSocialProvider(String providerId) {
		return socialProviderRepository.findByProviderId(providerId)
			.flatMap(socialProvider -> userRepository.findBySocialProvider(socialProvider)
				.map(WebtyUser::getUserId));
	}

	@Transactional
	public Long createUser(SocialProviderType socialProviderType, String providerId) {
		SocialProvider socialProvider = SocialProvider.builder()
			.provider(socialProviderType)
			.providerId(providerId)
			.build();
		socialProviderRepository.save(socialProvider);
		socialProviderRepository.flush();

		String nickname = generateUniqueNickname(socialProvider);
		WebtyUser webtyUser = WebtyUser.builder()
			.nickname(nickname)
			.profileImage(defaultProfileImagePath)
			.socialProvider(socialProvider)
			.build();

		userRepository.save(webtyUser);

		return webtyUser.getUserId();
	}

	public String generateUniqueNickname(SocialProvider socialProvider) {
		String uniqueNickname = DEFAULT_NICKNAME.formatted(UUID.randomUUID().toString().substring(0,18));
		// 닉네임이 만약 중복되었을 경우 값을 추가하는 기능 추가
		while (userRepository.existsByNickname(uniqueNickname)) {
			uniqueNickname = uniqueNickname.formatted(UUID.randomUUID().toString().substring(0,18));
		}
		return uniqueNickname;
	}

	@Transactional
	public void modifyNickname(WebtyUserDetails webtyUserDetails, String nickname) {
		WebtyUser webtyUser = authWebtyUserProvider.getAuthenticatedWebtyUser(webtyUserDetails);

		if (userRepository.existsByNickname(nickname)) {
			throw new BusinessException(ErrorCode.USER_NICKNAME_DUPLICATION);
		}
		webtyUser.modifyNickname(nickname);
		userRepository.save(webtyUser);
	}

	@Transactional
	public void modifyImage(WebtyUserDetails webtyUserDetails, MultipartFile file) throws IOException {
		WebtyUser webtyUser = authWebtyUserProvider.getAuthenticatedWebtyUser(webtyUserDetails);

		String filePath = fileStorageUtil.storeImageFile(file);

		webtyUser.updateProfile(webtyUser.getNickname(), filePath);
		userRepository.save(webtyUser);
	}

	@Transactional
	public void delete(WebtyUserDetails webtyUserDetails) {
		WebtyUser webtyUser = authWebtyUserProvider.getAuthenticatedWebtyUser(webtyUserDetails);

		WebtyUser existingUser = userRepository.findById(webtyUser.getUserId())
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		userRepository.delete(existingUser);
	}

	@Transactional
	public String findNickNameByUserId(Long userId) {
		WebtyUser webtyUser = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
		return webtyUser.getNickname();
	}

	public WebtyUser findByNickName(String nickName) {
		return userRepository.findByNickname(nickName)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}

	public WebtyUser getAuthenticatedUser(WebtyUserDetails webtyUserDetails) {
		return authWebtyUserProvider.getAuthenticatedWebtyUser(webtyUserDetails);
	}
}
