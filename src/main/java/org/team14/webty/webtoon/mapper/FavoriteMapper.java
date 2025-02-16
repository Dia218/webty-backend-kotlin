package org.team14.webty.webtoon.mapper;

import org.team14.webty.user.entity.WebtyUser;
import org.team14.webty.webtoon.entity.Favorite;
import org.team14.webty.webtoon.entity.Webtoon;

public class FavoriteMapper {
	public static Favorite toEntity(WebtyUser webtyUser, Webtoon webtoon) {
		return Favorite.builder()
			.webtyUser(webtyUser)
			.webtoon(webtoon)
			.build();
	}

}
