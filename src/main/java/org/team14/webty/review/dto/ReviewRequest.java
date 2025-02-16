package org.team14.webty.review.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import org.team14.webty.review.enumrate.SpoilerStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequest {
	private Long webtoonId;
	private String content;
	private String title;
	private SpoilerStatus spoilerStatus;
	@JsonIgnore
	private List<MultipartFile> images = new ArrayList<>(); // 이미지 파일 리스트 추가

	public void setImages(List<MultipartFile> images) {
		this.images = images;
	}
}
