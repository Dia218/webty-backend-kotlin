package org.team14.webty.recommend.entity

import jakarta.persistence.*
import org.team14.webty.recommend.enumerate.LikeType
import org.team14.webty.review.entity.Review

@Entity
data class Recommend(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vote_id")
    val voteId: Long? = null,

    @Enumerated(EnumType.STRING)
    val likeType: LikeType,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    val review: Review,

    @Column(name = "user_id")
    val userId: Long
)