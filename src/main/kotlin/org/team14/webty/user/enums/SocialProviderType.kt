package org.team14.webty.user.enums

import java.util.*

enum class SocialProviderType(
        val providerName: String
) {
    KAKAO("kakao");

    companion object {
        fun fromProviderName(provider: String): SocialProviderType {
            return Arrays.stream(entries.toTypedArray())
                    .filter { type: SocialProviderType ->
                        type.providerName.equals(provider, ignoreCase = true)
                    }
                    .findFirst()
                    .orElseThrow<IllegalArgumentException> {
                        IllegalArgumentException(
                                "지원하지 않는 로그인 제공자: $provider"
                        )
                    }
        }
    }
}
