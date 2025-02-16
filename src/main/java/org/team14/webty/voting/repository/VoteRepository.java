package org.team14.webty.voting.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.team14.webty.voting.entity.Similar;
import org.team14.webty.voting.entity.Vote;
import org.team14.webty.voting.enumerate.VoteType;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
	Optional<Vote> findBySimilarAndUserId(Similar similar, Long userId);

	Boolean existsByUserIdAndSimilar(Long userId, Similar similar);

	int deleteBySimilarAndUserId(Similar similar, Long userId);

	List<Vote> findAllBySimilar(Similar similar);

	long countBySimilarAndVoteType(Similar similar, VoteType voteType);
}
