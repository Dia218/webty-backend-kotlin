package org.team14.webty.security.authentication

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.team14.webty.user.entity.WebtyUser

class WebtyUserDetails(
    val webtyUser: WebtyUser
) : UserDetails {
    private val userRole = "ROLE_USER"

    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority(userRole))

    override fun getPassword(): String? = null

    override fun getUsername(): String = webtyUser.nickname

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}
