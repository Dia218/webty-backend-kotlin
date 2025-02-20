package org.team14.webty.recommend.entity

import jakarta.persistence.*
import org.team14.webty.recommend.enumerate.LikeType
import org.team14.webty.review.entity.Review

@Entity
class Recommend(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommend_id")
    var recommendId: Long? = null,
    
    @Enumerated(EnumType.STRING)
    val likeType: LikeType,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    val review: Review,
    
    @Column(name = "user_id")
    val userId: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Recommend) return false
        if (recommendId == null || other.recommendId == null) return false
        return recommendId == other.recommendId
    }
    
    override fun hashCode(): Int {
        return recommendId.hashCode()
    }
    
}