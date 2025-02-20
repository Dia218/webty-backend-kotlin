package org.team14.webty.user.entity

import jakarta.persistence.*
import org.team14.webty.user.enums.SocialProviderType

@Entity
@Table(name = "social_provider")
class SocialProvider(
        @Enumerated(EnumType.STRING)
        @Column(name = "provider", nullable = false)
        val provider: SocialProviderType,

        @Column(name = "provider_id", nullable = false)
        val providerId: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var socialId: Long? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SocialProvider

        return socialId == other.socialId
    }

    override fun hashCode(): Int {
        return socialId?.hashCode() ?: 0
    }
}
