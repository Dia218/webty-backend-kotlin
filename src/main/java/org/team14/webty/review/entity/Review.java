package org.team14.webty.review.entity;

import java.time.LocalDateTime;

import org.team14.webty.review.enumrate.SpoilerStatus;
import org.team14.webty.user.entity.WebtyUser;
import org.team14.webty.webtoon.entity.Webtoon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "review")
public class Review {
	@Enumerated(EnumType.STRING)
	public SpoilerStatus isSpoiler;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "review_id")
	private Long reviewId;
	@ManyToOne
	@JoinColumn(name = "userId")
	private WebtyUser user;
	@Column(length = 5000)
	private String content;
	private String title;
	@Column(columnDefinition = "integer default 0", nullable = false)
	private Integer viewCount;
	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	@ManyToOne
	@JoinColumn(name = "webtoonId")
	private Webtoon webtoon;

	public void plusViewCount() {
		this.viewCount++;
	}

	public void updateReview(String title, String content, SpoilerStatus isSpoiler, Webtoon webtoon) {
		this.title = title;
		this.content = content;
		this.isSpoiler = isSpoiler;
		this.webtoon = webtoon;
		this.updatedAt = LocalDateTime.now();
	}

	public void patchIsSpoiler() {
		this.isSpoiler = SpoilerStatus.TRUE;
	}
}
