package org.team14.webty.user.entity

import jakarta.persistence.*

@Entity
@Table(name = "webty_user")
class WebtyUser(
        @Column(name = "nickname", nullable = false, unique = true)
        val nickname: String,

        @Column(name = "profile_image")
        val profileImage: String,

        @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
        @JoinColumn(name = "social_provider_id")
        val socialProvider: SocialProvider? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var userId: Long? = null

    // 프로필 업데이트를 위한 비즈니스 메서드
    fun updateProfile(nickname: String? = null, profileImage: String? = null): WebtyUser {
        return copy(nickname, profileImage)
    }

    private fun copy(
            nickname: String? = null,
            profileImage: String? = null,
    ): WebtyUser {
        val copiedWebtyUser = WebtyUser(
                nickname = nickname ?: this.nickname, // 변경 가능한 값
                profileImage = profileImage ?: this.profileImage, // 변경 가능한 값
                socialProvider = this.socialProvider
        )
        copiedWebtyUser.userId = this.userId
        return copiedWebtyUser
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WebtyUser

        return userId == other.userId
    }

    override fun hashCode(): Int {
        return userId?.hashCode() ?: 0
    }
}
