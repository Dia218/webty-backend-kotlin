package org.team14.webty.review.entity

import jakarta.persistence.*
import org.team14.webty.review.enumrate.SpoilerStatus
import org.team14.webty.user.entity.WebtyUser
import org.team14.webty.webtoon.entity.Webtoon
import java.time.LocalDateTime

@Entity
@Table(name = "review")
data class Review(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    val reviewId: Long? = null,

    @ManyToOne
    @JoinColumn(name = "userId")
    val user: WebtyUser,

    @ManyToOne
    @JoinColumn(name = "webtoonId")
    var webtoon: Webtoon,

    @Column(length = 5000)
    var content: String,

    var title: String,

    @Enumerated(EnumType.STRING)
    var isSpoiler: SpoilerStatus,

    @Column(columnDefinition = "integer default 0", nullable = false)
    var viewCount: Int = 0,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime? = null
) {
    fun plusViewCount() {
        viewCount++
    }

    fun updateReview(title: String, content: String, isSpoiler: SpoilerStatus, webtoon: Webtoon) {
        this.title = title
        this.content = content
        this.isSpoiler = isSpoiler
        this.webtoon = webtoon
        this.updatedAt = LocalDateTime.now()
    }

    fun patchIsSpoiler() {
        this.isSpoiler = SpoilerStatus.TRUE
    }


}