package org.team14.webty.voting.dto;

import lombok.Getter;

@Getter
public class VoteRequest {
	private Long similarId;
	private String voteType;
}
