package org.team14.webty.webtoon.entity

import jakarta.persistence.*
import org.team14.webty.user.entity.WebtyUser


@Entity
data class Favorite (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var favoriteId: Long? = null,

    @ManyToOne
    @JoinColumn(name = "userId")
    val webtyUser: WebtyUser,

    @ManyToOne
    @JoinColumn(name = "webtoonId")
    val webtoon: Webtoon

)


