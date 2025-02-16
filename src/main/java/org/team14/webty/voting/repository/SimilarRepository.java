package org.team14.webty.voting.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.team14.webty.voting.entity.Similar;
import org.team14.webty.webtoon.entity.Webtoon;

@Repository
public interface SimilarRepository extends JpaRepository<Similar, Long> {
	Boolean existsByWebtoonAndSimilarWebtoonName(Webtoon webtoon, String similarWebtoonName);

	Optional<Similar> findByUserIdAndSimilarId(Long userId, Long similarId);

	Page<Similar> findAllByWebtoon(Webtoon webtoon, Pageable pageable);
}
