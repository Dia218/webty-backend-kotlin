package org.team14.webty.voting.entity

import jakarta.persistence.*
import org.team14.webty.voting.enums.VoteType

@Entity
class Vote(
    val userId: Long,

    @ManyToOne
    @JoinColumn(name = "similarId")
    val similar: Similar,

    @Enumerated(value = EnumType.STRING)
    val voteType: VoteType
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var voteId: Long? = null
        private set

    fun copy(
        voteType: VoteType,
    ): Vote {
        val copiedVote = Vote(
            userId = this.userId,
            similar = this.similar,
            voteType = voteType // 변경 가능한 값
        )
        copiedVote.voteId = this.voteId
        return copiedVote
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vote

        return voteId == other.voteId
    }

    override fun hashCode(): Int {
        return voteId?.hashCode() ?: 0
    }
}
