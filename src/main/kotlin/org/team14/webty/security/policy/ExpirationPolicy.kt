package org.team14.webty.security.policy

object ExpirationPolicy {
    const val refreshTokenExpirationTime: Long = 3600000 // 1 hour in milliseconds
    const val accessTokenExpirationTime: Long = 3600000 // 1 hour in milliseconds
}
