package org.team14.webty.review.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.team14.webty.review.entity.Review;
import org.team14.webty.review.entity.ReviewImage;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
	List<ReviewImage> findAllByReview(Review review);

	@Query("SELECT ri FROM ReviewImage ri WHERE ri.review.reviewId IN :reviewIds")
	List<ReviewImage> findByReviewIdIn(@Param("reviewIds") List<Long> reviewIds);

}
