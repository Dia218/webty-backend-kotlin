package org.team14.webty.review.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.team14.webty.review.entity.Review
import org.team14.webty.user.entity.WebtyUser

@Repository
interface ReviewRepository : JpaRepository<Review, Long> {
    @Query("SELECT r FROM Review r WHERE r.user = :webtyUser")
    fun findReviewByWebtyUser(@Param("webtyUser") webtyUser: WebtyUser?): List<Review> // 특정 사용자의 리뷰 목록 조회
    
    @Query("SELECT r FROM Review r WHERE r.user = :webtyUser ORDER BY r.reviewId DESC")
    fun findReviewByWebtyUser(
        @Param("webtyUser") webtyUser: WebtyUser?,
        pageable: Pageable?
    ): Page<Review> // 특정 사용자의 리뷰 목록 조회 (페이징 처리)
    
    // 조회수 내림차순으로 모든 리뷰 조회
    fun findAllByOrderByViewCountDesc(pageable: Pageable?): Page<Review>
    
    fun findAllByOrderByReviewIdDesc(pageable: Pageable?): Page<Review> // 리뷰 ID 내림차순으로 모든 리뷰 조회
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.user = :webtyUser")
    fun countReviewByWebtyUser(@Param("webtyUser") webtyUser: WebtyUser): Long // 특정 사용자의 리뷰 개수 조회
    
    @Query("SELECT r FROM Review r WHERE LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%')) ORDER BY r.reviewId DESC")
    fun findByTitleContainingIgnoreCaseOrderByReviewIdDesc(
        @Param("title") title: String?,
        pageable: Pageable?
    ): Page<Review>
    
    @Query("SELECT r FROM Review r WHERE r.webtoon.webtoonId = :webtoonId ORDER BY r.reviewId DESC")
    fun findReviewByWebtoonId(@Param("webtoonId") webtoonId: Long?, pageable: Pageable?): Page<Review>
}
