package org.team14.webty.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.team14.webty.user.entity.SocialProvider
import java.util.*

@Repository
interface SocialProviderRepository : JpaRepository<SocialProvider, Long> {
    fun findByProviderId(providerId: String): Optional<SocialProvider>
}
