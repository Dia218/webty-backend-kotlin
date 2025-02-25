package org.team14.webty.voting.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.team14.webty.voting.entity.Similar
import org.team14.webty.webtoon.entity.Webtoon
import java.util.*


@Repository
interface SimilarRepository : JpaRepository<Similar, Long> {
    fun existsByTargetWebtoonAndSimilarWebtoonId(targetWebtoon: Webtoon, similarWebtoonId: Long): Boolean

    fun findByUserIdAndSimilarId(userId: Long, similarId: Long): Optional<Similar>

    @Query("SELECT s FROM Similar s WHERE s.targetWebtoon = :targetWebtoon ORDER BY s.similarResult DESC")
    fun findAllByTargetWebtoon(targetWebtoon: Webtoon, pageable: Pageable): Page<Similar>
}
