package org.team14.webty.webtoon.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.team14.webty.webtoon.entity.Webtoon
import org.team14.webty.webtoon.enumerate.Platform
import java.util.*

@Repository
interface WebtoonRepository : JpaRepository<Webtoon, Long> {
    
    @Query(
        """
        SELECT w FROM Webtoon w 
        WHERE (:webtoonName IS NULL OR w.webtoonName LIKE %:webtoonName%) 
        AND (:platform IS NULL OR w.platform = :platform) 
        AND (:authors IS NULL OR w.authors LIKE %:authors%) 
        AND (:finished IS NULL OR w.finished = :finished)
    """
    )
    fun searchWebtoons(
        @Param("webtoonName") webtoonName: String?,
        @Param("platform") platform: Platform?,
        @Param("authors") authors: String?,
        @Param("finished") finished: Boolean?,
        pageable: Pageable
    ): Page<Webtoon>
    
    fun findByWebtoonName(similarWebtoonName: String): Optional<Webtoon>
}
