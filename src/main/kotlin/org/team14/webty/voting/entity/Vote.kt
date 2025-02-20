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
    var voteType: VoteType? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var voteId: Long? = null
        private set
}
