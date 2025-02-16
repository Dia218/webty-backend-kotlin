package org.team14.webty.webtoon.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebtoonSearchRequest {
	@Size(max = 100)
	private String webtoonName;

	@Pattern(regexp = "^(NAVER|KAKAO_PAGE)?$", message = "Platform must be either NAVER or KAKAO_PAGE")
	private String platform;

	@Size(max = 100)
	private String authors;

	private Boolean finished;

	@Min(0)
	private int page = 0;

	@Min(1)
	@Max(100)
	private int size = 10;

	@Pattern(regexp = "^(WEBTOON_NAME|AUTHORS|PLATFORM|FINISHED)$",
		message = "Sort by must be one of: WEBTOON_NAME, PLATFORM, AUTHORS, FINISHED")
	private String sortBy = "WEBTOON_NAME";

	@Pattern(regexp = "^(asc|desc)$", message = "Sort direction must be either asc or desc")
	private String sortDirection = "asc";
}