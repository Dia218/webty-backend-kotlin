package org.team14.webty.webtoon.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.team14.webty.user.entity.WebtyUser;
import org.team14.webty.webtoon.entity.Favorite;
import org.team14.webty.webtoon.entity.Webtoon;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

	Optional<Favorite> findByWebtyUserAndWebtoon(WebtyUser webtyUser, Webtoon webtoon);

	void deleteByWebtyUserAndWebtoon(WebtyUser webtyUser, Webtoon webtoon);

	List<Favorite> findByWebtyUser(WebtyUser user);

}
