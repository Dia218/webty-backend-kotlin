package org.team14.webty.voting.mapper;

import org.team14.webty.user.entity.WebtyUser;
import org.team14.webty.voting.entity.Similar;
import org.team14.webty.voting.entity.Vote;
import org.team14.webty.voting.enumerate.VoteType;

public class VoteMapper {
	public static Vote toEntity(WebtyUser webtyUser, Similar similar, String type) {
		return Vote.builder()
			.userId(webtyUser.getUserId())
			.similar(similar)
			.voteType(VoteType.fromString(type))
			.build();
	}
}
