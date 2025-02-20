package org.team14.webty.review.entity

import jakarta.persistence.*
import org.team14.webty.common.entity.BaseEntity
import org.team14.webty.review.enumrate.SpoilerStatus
import org.team14.webty.user.entity.WebtyUser
import org.team14.webty.webtoon.entity.Webtoon

@Entity
@Table(name = "review")
class Review(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    var reviewId: Long? = null,

    @ManyToOne
    @JoinColumn(name = "userId")
    val user: WebtyUser,

    @ManyToOne
    @JoinColumn(name = "webtoonId")
    val webtoon: Webtoon,

    @Column(length = 5000)
    val content: String,

    val title: String,

    @Enumerated(EnumType.STRING)
    val isSpoiler: SpoilerStatus,

    @Column(columnDefinition = "integer default 0", nullable = false)
    val viewCount: Int = 0,

) : BaseEntity() {
    fun plusViewCount(): Review {
        val newReview = Review(
            reviewId = this.reviewId,
            user = this.user,
            webtoon = this.webtoon,
            content = this.content,
            title = this.title,
            isSpoiler = this.isSpoiler,
            viewCount = this.viewCount + 1
        )
        newReview.createdAt = this.createdAt
        newReview.modifiedAt = this.modifiedAt
        return newReview
    }

    fun updatedReview(title: String, content: String, isSpoiler: SpoilerStatus, webtoon: Webtoon): Review {
        val newReview = Review(
            reviewId = this.reviewId,
            user = this.user,
            webtoon = webtoon,
            content = content,
            title = title,
            isSpoiler = isSpoiler,
            viewCount = this.viewCount
        )
        newReview.createdAt = this.createdAt
        newReview.modifiedAt = this.modifiedAt
        return newReview
    }

    fun patchedIsSpoiler(): Review {
        val newReview = Review(
            reviewId = this.reviewId,
            user = this.user,
            webtoon = this.webtoon,
            content = this.content,
            title = this.title,
            isSpoiler = SpoilerStatus.TRUE,
            viewCount = this.viewCount
        )
        newReview.createdAt = this.createdAt
        newReview.modifiedAt = this.modifiedAt
        return newReview
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Review) return false

        return reviewId == other.reviewId
    }

    override fun hashCode(): Int {
        return reviewId?.hashCode() ?: 0
    }
}