package org.team14.webty.reviewComment.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.team14.webty.user.dto.UserDataResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
	private UserDataResponse user;
	private Long commentId;
	private String content;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;
	private Integer depth;
	private List<String> mentions;

	@Builder.Default
	private List<CommentResponse> childComments = new ArrayList<>();
} 