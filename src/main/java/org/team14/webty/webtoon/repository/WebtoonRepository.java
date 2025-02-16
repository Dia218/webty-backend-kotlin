package org.team14.webty.webtoon.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.team14.webty.webtoon.entity.Webtoon;
import org.team14.webty.webtoon.enumerate.Platform;

@Repository
public interface WebtoonRepository extends JpaRepository<Webtoon, Long> {

	@Query("SELECT w FROM Webtoon w " +
		"WHERE (:webtoonName IS NULL OR w.webtoonName LIKE %:webtoonName%) " +
		"AND (:platform IS NULL OR w.platform = :platform) " +
		"AND (:authors IS NULL OR w.authors LIKE %:authors%) " +
		"AND (:finished IS NULL OR w.finished = :finished)")
	Page<Webtoon> searchWebtoons(@Param("webtoonName") String webtoonName,
		@Param("platform") Platform platform,
		@Param("authors") String authors,
		@Param("finished") Boolean finished,
		Pageable pageable);

	Optional<Webtoon> findByWebtoonName(String similarWebtoonName);
}
