package org.team14.webty.security.oauth2

class ProviderUserInfo(private val attributes: Map<String, Any>) {
    val providerId: String
        get() = attributes["id"].toString()
}