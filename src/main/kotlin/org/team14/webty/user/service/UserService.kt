package org.team14.webty.user.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.team14.webty.common.exception.BusinessException
import org.team14.webty.common.exception.ErrorCode
import org.team14.webty.common.util.FileStorageUtil
import org.team14.webty.security.authentication.AuthWebtyUserProvider
import org.team14.webty.security.authentication.WebtyUserDetails
import org.team14.webty.user.entity.SocialProvider
import org.team14.webty.user.entity.WebtyUser
import org.team14.webty.user.enums.SocialProviderType
import org.team14.webty.user.repository.SocialProviderRepository
import org.team14.webty.user.repository.UserRepository
import java.io.IOException
import java.util.*
import java.util.function.Function
import java.util.function.Supplier

@Service
class UserService(
        @Value("\${default-profile-image}")
        private val defaultProfileImagePath: String,
        private val userRepository: UserRepository,
        private val socialProviderRepository: SocialProviderRepository,
        private val authWebtyUserProvider: AuthWebtyUserProvider,
        private val fileStorageUtil: FileStorageUtil
) {
    @Transactional(readOnly = true)
    fun existSocialProvider(providerId: String): Optional<Long> {
        return socialProviderRepository.findByProviderId(providerId)
                .flatMap(Function<SocialProvider, Optional<out Long?>> { socialProvider: SocialProvider ->
                    userRepository.findBySocialProvider(socialProvider)
                            .map(WebtyUser::userId)
                })
    }

    @Transactional
    fun createUser(socialProviderType: SocialProviderType, providerId: String): Long {
        val socialProvider = SocialProvider(
                provider = socialProviderType,
                providerId = providerId
        )
        socialProviderRepository.save(socialProvider)
        socialProviderRepository.flush()
        val nickname = generateUniqueNickname(socialProvider)
        val webtyUser = WebtyUser(
                nickname = nickname,
                profileImage = defaultProfileImagePath,
                socialProvider = socialProvider
        )
        userRepository.save(webtyUser)
        return webtyUser.userId!!
    }

    fun generateUniqueNickname(socialProvider: SocialProvider): String {
        var uniqueNickname = DEFAULT_NICKNAME.formatted(UUID.randomUUID().toString().substring(0, 18))
        // 닉네임이 만약 중복되었을 경우 값을 추가하는 기능 추가
        while (userRepository.existsByNickname(uniqueNickname)) {
            uniqueNickname = uniqueNickname.formatted(UUID.randomUUID().toString().substring(0, 18))
        }
        return uniqueNickname
    }

    @Transactional
    fun modifyNickname(webtyUserDetails: WebtyUserDetails, nickname: String) {
        val webtyUser = authWebtyUserProvider.getAuthenticatedWebtyUser(webtyUserDetails)
        if (userRepository.existsByNickname(nickname)) {
            throw BusinessException(ErrorCode.USER_NICKNAME_DUPLICATION)
        }
        webtyUser.updateProfile(nickname = nickname)
        userRepository.save(webtyUser)
    }

    @Transactional
    @Throws(IOException::class)
    fun modifyImage(webtyUserDetails: WebtyUserDetails, file: MultipartFile) {
        val webtyUser = authWebtyUserProvider.getAuthenticatedWebtyUser(webtyUserDetails)
        val filePath = fileStorageUtil.storeImageFile(file)
        webtyUser.updateProfile(webtyUser.nickname, filePath)
        userRepository.save(webtyUser)
    }

    @Transactional
    fun delete(webtyUserDetails: WebtyUserDetails) {
        val webtyUser = authWebtyUserProvider.getAuthenticatedWebtyUser(webtyUserDetails)
        val existingUser = userRepository.findById(webtyUser.userId!!)
                .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }!!
        userRepository.delete(existingUser)
    }

    @Transactional
    fun findNickNameByUserId(userId: Long): String {
        val webtyUser = userRepository.findById(userId)
                .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }!!
        return webtyUser.nickname
    }

    fun findByNickName(nickName: String): WebtyUser {
        return userRepository.findByNickname(nickName)
                .orElseThrow(Supplier { BusinessException(ErrorCode.USER_NOT_FOUND) })!!
    }

    fun getAuthenticatedUser(webtyUserDetails: WebtyUserDetails): WebtyUser {
        return authWebtyUserProvider.getAuthenticatedWebtyUser(webtyUserDetails)
    }

    companion object {
        private const val DEFAULT_NICKNAME = "웹티사랑꾼 %s"
    }
}
