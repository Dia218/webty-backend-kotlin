package org.team14.webty.webtoon.entity

import jakarta.persistence.*
import org.team14.webty.webtoon.enumerate.Platform


@Entity
data class Webtoon(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val webtoonId: Long? = null,
    
    var webtoonName: String,
    
    @Enumerated(EnumType.STRING)
    var platform: Platform,
    
    var webtoonLink: String,
    
    var thumbnailUrl: String,
    
    var authors: String,
    
    var finished: Boolean
)
