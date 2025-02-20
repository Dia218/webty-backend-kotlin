package org.team14.webty.reviewComment.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.team14.webty.common.entity.BaseEntity;
import org.team14.webty.review.entity.Review;
import org.team14.webty.user.entity.WebtyUser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "review_comment", indexes = {
	@Index(name = "idx_review_comment", columnList = "review_id, depth, comment_id DESC"),
	@Index(name = "idx_parent_comment", columnList = "parent_id, comment_id ASC")
})
public class ReviewComment extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "review_id")
	public Review review;
	// Adjacency List 방식으로 변경
	@Column(name = "parent_id")
	public Long parentId;  // 부모 댓글의 ID를 직접 저장
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "comment_id")
	private Long commentId;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private WebtyUser user;
	@Column(name = "content", nullable = false)
	private String content;
	@Column(name = "depth")
	@Builder.Default
	private Integer depth = 0;  // 댓글의 깊이 (0: 루트 댓글, 1: 대댓글)

	@Convert(converter = ListToJsonConverter.class)
	@Builder.Default
	private List<String> mentions = new ArrayList<>();

	public void updateComment(String comment) {
		this.content = comment;
	}

	@Converter
	public static class ListToJsonConverter implements AttributeConverter<List<String>, String> {

		private final ObjectMapper objectMapper = new ObjectMapper();

		@Override
		public String convertToDatabaseColumn(List<String> attribute) {
			try {
				return objectMapper.writeValueAsString(attribute);
			} catch (IOException e) {
				throw new RuntimeException("Failed to convert list to JSON", e);
			}
		}

		@Override
		public List<String> convertToEntityAttribute(String dbData) {
			try {
				return objectMapper.readValue(dbData, new TypeReference<>() {
				});
			} catch (IOException e) {
				throw new RuntimeException("Failed to convert JSON to list", e);
			}
		}
	}
}
