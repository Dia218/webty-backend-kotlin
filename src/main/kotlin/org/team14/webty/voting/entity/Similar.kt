package org.team14.webty.voting.entity

import jakarta.persistence.*
import org.team14.webty.webtoon.entity.Webtoon

@Entity
@Table(name = "similar", uniqueConstraints = [UniqueConstraint(columnNames = ["targetWebtoonId", "similarWebtoonId"])])
class Similar(
        val similarWebtoonId: Long,
        var similarResult: Long,
        val userId: Long,

        @ManyToOne
        @JoinColumn(name = "targetWebtoonId")
        val targetWebtoon: Webtoon
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var similarId: Long? = null
        private set

    fun updateSimilarResult(similarResult: Long) {
        this.similarResult = similarResult
    }
}
