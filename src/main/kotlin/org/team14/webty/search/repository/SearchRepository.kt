package org.team14.webty.search.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.team14.webty.review.entity.Review

@Repository
interface SearchRepository : JpaRepository<Review, Long> {
    
    // 키워드로 리뷰를 검색하는 메서드입니다.
    // 웹툰 이름, 리뷰 내용, 사용자 닉네임 중에 키워드가 포함된 리뷰를 최신순으로 정렬합니다.
    @Query(SearchQueryBuilder.BASE_QUERY + SearchQueryBuilder.KEYWORD_CONDITION + SearchQueryBuilder.LATEST_ORDER_BY)
    fun searchByKeyword(@Param("keyword") keyword: String?, pageable: Pageable): Page<Review>
    
    // 키워드로 검색하고 조회수 기준으로 정렬하는 메서드입니다.
    @Query(SearchQueryBuilder.BASE_QUERY + SearchQueryBuilder.KEYWORD_CONDITION + SearchQueryBuilder.VIEW_COUNT_ORDER_BY)
    fun searchByKeywordOrderByViewCount(@Param("keyword") keyword: String?, pageable: Pageable): Page<Review>
    
    // 키워드로 검색하고 좋아요(추천수) 기준으로 정렬하는 메서드입니다.
    // 좋아요가 있는 리뷰만 선택하여 좋아요 수 내림차순으로 정렬합니다.
    @Query(value = SearchQueryBuilder.BASE_QUERY + SearchQueryBuilder.KEYWORD_CONDITION + SearchQueryBuilder.RECOMMEND_COUNT_ORDER_BY)
    fun searchByKeywordOrderByRecommendCount(@Param("keyword") keyword: String?, pageable: Pageable): Page<Review>
    
    // 웹툰 이름으로 검색
    @Query(SearchQueryBuilder.BASE_QUERY + SearchQueryBuilder.WEBTOON_NAME_CONDITION + SearchQueryBuilder.LATEST_ORDER_BY)
    fun searchByWebtoonName(@Param("keyword") keyword: String?, pageable: Pageable): Page<Review>
    
    // 웹툰 이름으로 검색 (추천수 정렬)
    @Query(value = SearchQueryBuilder.BASE_QUERY + SearchQueryBuilder.WEBTOON_NAME_CONDITION + SearchQueryBuilder.RECOMMEND_COUNT_ORDER_BY)
    fun searchByWebtoonNameOrderByRecommendCount(@Param("keyword") keyword: String?, pageable: Pageable): Page<Review>
    
    // 사용자 닉네임으로 검색
    @Query(SearchQueryBuilder.BASE_QUERY + SearchQueryBuilder.NICKNAME_CONDITION + SearchQueryBuilder.LATEST_ORDER_BY)
    fun searchByNickname(@Param("keyword") keyword: String?, pageable: Pageable): Page<Review>
    
    // 사용자 닉네임으로 검색 (추천수 정렬)
    @Query(value = SearchQueryBuilder.BASE_QUERY + SearchQueryBuilder.NICKNAME_CONDITION + SearchQueryBuilder.RECOMMEND_COUNT_ORDER_BY)
    fun searchByNicknameOrderByRecommendCount(@Param("keyword") keyword: String?, pageable: Pageable): Page<Review>
    
    // 리뷰 내용 및 제목으로 검색
    @Query(SearchQueryBuilder.BASE_QUERY + SearchQueryBuilder.REVIEW_CONTENT_CONDITION + SearchQueryBuilder.LATEST_ORDER_BY)
    fun searchByReviewContent(@Param("keyword") keyword: String?, pageable: Pageable): Page<Review>
    
    // 리뷰 내용 및 제목으로 검색 (추천수 정렬)
    @Query(value = SearchQueryBuilder.BASE_QUERY + SearchQueryBuilder.REVIEW_CONTENT_CONDITION + SearchQueryBuilder.RECOMMEND_COUNT_ORDER_BY)
    fun searchByReviewContentOrderByRecommendCount(@Param("keyword") keyword: String?, pageable: Pageable): Page<Review>
} 