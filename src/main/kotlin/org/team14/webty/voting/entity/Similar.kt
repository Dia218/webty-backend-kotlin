package org.team14.webty.voting.entity

import jakarta.persistence.*
import org.team14.webty.webtoon.entity.Webtoon

@Entity
@Table(name = "similar", uniqueConstraints = [UniqueConstraint(columnNames = ["targetWebtoonId", "similarWebtoonId"])])
class Similar(
    val similarWebtoonId: Long,
    val similarResult: Long,
    val userId: Long,

    @ManyToOne
    @JoinColumn(name = "targetWebtoonId")
    val targetWebtoon: Webtoon
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var similarId: Long? = null
        private set

    fun copy(
        similarResult: Long? = null,
    ): Similar {
        val copiedSimilar = Similar(
            similarWebtoonId = this.similarWebtoonId,
            similarResult = similarResult ?: this.similarResult, // 변경 가능한 값
            userId = this.userId,
            targetWebtoon = this.targetWebtoon
        )
        copiedSimilar.similarId = this.similarId
        return copiedSimilar
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Similar

        return similarId == other.similarId
    }

    override fun hashCode(): Int {
        return similarId?.hashCode() ?: 0
    }
}
