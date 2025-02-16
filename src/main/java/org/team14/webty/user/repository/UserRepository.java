package org.team14.webty.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.team14.webty.user.entity.SocialProvider;
import org.team14.webty.user.entity.WebtyUser;

@Repository
public interface UserRepository extends JpaRepository<WebtyUser, Long> {
	Optional<WebtyUser> findByNickname(String nickname);

	Optional<WebtyUser> findBySocialProvider(SocialProvider socialProvider);

	boolean existsByNickname(String nickname);
}
