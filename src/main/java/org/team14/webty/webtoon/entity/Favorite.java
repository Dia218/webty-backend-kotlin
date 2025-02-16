package org.team14.webty.webtoon.entity;

import org.team14.webty.user.entity.WebtyUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Favorite {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long favoriteId;

	@ManyToOne
	@JoinColumn(name = "userId")
	private WebtyUser webtyUser;

	@ManyToOne
	@JoinColumn(name = "webtoonId")
	private Webtoon webtoon;

}
