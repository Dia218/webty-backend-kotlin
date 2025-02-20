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
    private fun copy(
        webtoon: Webtoon = this.webtoon,
        content: String = this.content,
        title: String = this.title,
        isSpoiler: SpoilerStatus = this.isSpoiler,
        viewCount: Int = this.viewCount
    ): Review {
        return Review(
            reviewId = reviewId,
            user = user,
            webtoon = webtoon,
            content = content,
            title = title,
            isSpoiler = isSpoiler,
            viewCount = viewCount
        ).apply {
            this.createdAt = this@Review.createdAt
            this.modifiedAt = this@Review.modifiedAt
        }
    }

    fun plusViewCount(): Review {
        return copy(viewCount = this.viewCount + 1)
    }

    fun updatedReview(title: String, content: String, isSpoiler: SpoilerStatus, webtoon: Webtoon): Review {
        return copy(title = title, content = content, isSpoiler = isSpoiler, webtoon = webtoon)
    }

    fun patchedIsSpoiler(): Review {
        return copy(isSpoiler = SpoilerStatus.TRUE)
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