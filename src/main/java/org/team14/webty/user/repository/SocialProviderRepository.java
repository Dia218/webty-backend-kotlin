package org.team14.webty.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.team14.webty.user.entity.SocialProvider;

@Repository
public interface SocialProviderRepository extends JpaRepository<SocialProvider, Long> {
	Optional<SocialProvider> findByProviderId(String providerId);
}
