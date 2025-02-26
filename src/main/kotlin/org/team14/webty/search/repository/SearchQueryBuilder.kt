package org.team14.webty.search.repository

/**
 * 검색 쿼리 상수를 정의하는 유틸리티 클래스입니다.
 * 참고: @Query 어노테이션에서는 컴파일 타임 상수만 사용할 수 있으므로,
 * 이 클래스의 메서드는 직접 호출할 수 없고 참조용으로만 사용합니다.
 */
object SearchQueryBuilder {
    /**
     * 기본 검색 쿼리 상수
     */
    const val BASE_QUERY = """
        SELECT DISTINCT r FROM Review r
        JOIN FETCH r.user u
        JOIN FETCH r.webtoon w
    """
    
    /**
     * 키워드 검색 조건 상수
     */
    const val KEYWORD_CONDITION = """
        WHERE (:keyword IS NULL OR 
            LOWER(w.webtoonName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR 
            LOWER(r.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR 
            LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """
    
    /**
     * 웹툰 이름 검색 조건 상수
     */
    const val WEBTOON_NAME_CONDITION = """
        WHERE LOWER(w.webtoonName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """
    
    /**
     * 닉네임 검색 조건 상수
     */
    const val NICKNAME_CONDITION = """
        WHERE LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """
    
    /**
     * 리뷰 내용 검색 조건 상수
     */
    const val REVIEW_CONTENT_CONDITION = """
        WHERE LOWER(r.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR 
              LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """
    
    /**
     * 최신순 정렬 조건 상수
     */
    const val LATEST_ORDER_BY = "ORDER BY r.reviewId DESC"
    
    /**
     * 조회수 정렬 조건 상수
     */
    const val VIEW_COUNT_ORDER_BY = "ORDER BY r.viewCount DESC"
    
    /**
     * 추천수 정렬 조건 상수
     */
    const val RECOMMEND_COUNT_ORDER_BY = """
        ORDER BY (
            SELECT COUNT(rec.recommendId) FROM Recommend rec 
            WHERE rec.review.reviewId = r.reviewId AND rec.likeType = 'LIKE'
        ) DESC
    """
} 