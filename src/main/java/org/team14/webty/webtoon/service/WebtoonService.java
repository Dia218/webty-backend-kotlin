package org.team14.webty.webtoon.service;

import static org.team14.webty.webtoon.mapper.WebtoonApiResponseMapper.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.team14.webty.common.exception.BusinessException;
import org.team14.webty.common.exception.ErrorCode;
import org.team14.webty.webtoon.api.WebtoonPageApiResponse;
import org.team14.webty.webtoon.entity.Webtoon;
import org.team14.webty.webtoon.enumerate.Platform;
import org.team14.webty.webtoon.enumerate.WebtoonSort;
import org.team14.webty.webtoon.mapper.WebtoonApiResponseMapper;
import org.team14.webty.webtoon.repository.WebtoonRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebtoonService {
	private static final String URL_QUERY_TEMPLATE = "https://korea-webtoon-api-cc7dda2f0d77.herokuapp.com/webtoons?page=%s&perPage=%s&sort=%s&provider=%s";
	private static final int DEFAULT_PAGE_SIZE = 100;
	private static final int DEFAULT_PAGE_NUMBER = 1;
	private static final String DEFAULT_SORT = "ASC";

	private final WebtoonRepository webtoonRepository;
	private final RestTemplate restTemplate;

	@Transactional
	public void saveWebtoons() {
		for (Platform provider : Platform.values()) {
			try {
				saveWebtoonsByProvider(provider);
			} catch (Exception e) {
				log.error("웹툰 저장 중 오류 발생 - Provider: {}, Error: {}", provider, e.getMessage(), e);
			}
		}
		log.info("모든 데이터 저장 완료");
	}

	private void saveWebtoonsByProvider(Platform provider) {
		boolean isLastPage = false;
		int page = DEFAULT_PAGE_NUMBER;

		do {
			log.info(String.valueOf(page));
			WebtoonPageApiResponse webtoonPageApiResponse = getWebtoonPageApiResponse(page, DEFAULT_PAGE_SIZE, DEFAULT_SORT, provider);
			saveWebtoonsFromPage(webtoonPageApiResponse);
			isLastPage = webtoonPageApiResponse.isLastPage();
			page++;
		} while (!isLastPage);
	}

	private WebtoonPageApiResponse getWebtoonPageApiResponse(int page, int perPage, String sort, Platform provider) {
		String url = String.format(URL_QUERY_TEMPLATE, page, perPage, sort, provider.getPlatformName());
		log.info(url);
		try {
			return restTemplate.getForObject(url, WebtoonPageApiResponse.class);
		} catch (RestClientException e) {
			log.error("API 요청 실패 - URL: {}, Error: {}", url, e.getMessage(), e);
			return null;
		}
	}

	private void saveWebtoonsFromPage(WebtoonPageApiResponse webtoonPageApiResponse) {

		List<Webtoon> webtoons = webtoonPageApiResponse.getWebtoonApiResponses()
			.stream()
			.map(WebtoonApiResponseMapper::toEntity)
			.collect(Collectors.toList());

		webtoonRepository.saveAll(webtoons);
	}

	@Scheduled(cron = "0 0 6 * * ?", zone = "Asia/Seoul")
	public void updateWebtoons() {
		log.info("웹툰 데이터 업데이트 시작 (비동기)");

		for (Platform provider : Platform.values()) {
			updateWebtoonsByProviderAsync(provider);
		}

		log.info("웹툰 데이터 업데이트 요청 완료");
	}

	@Async
	@Transactional
	public void updateWebtoonsByProviderAsync(Platform provider) {
		try {
			Set<String> existingWebtoonKeys = webtoonRepository.findAll()
				.stream()
				.map(webtoon->generateWebtoonKey(webtoon))
				.collect(Collectors.toSet());

			updateWebtoonsByProvider(provider, existingWebtoonKeys);
		} catch (Exception e) {
			log.error("웹툰 업데이트 중 오류 발생 - Provider: {}, Error: {}", provider, e.getMessage(), e);
		}
	}

	private void updateWebtoonsByProvider(Platform provider, Set<String> existingWebtoonKeys) {
		boolean isLastPage = false;
		int page = DEFAULT_PAGE_NUMBER;

		do {
			WebtoonPageApiResponse webtoonPageApiResponse = getWebtoonPageApiResponse(page, DEFAULT_PAGE_SIZE, DEFAULT_SORT, provider);

			if (webtoonPageApiResponse == null || webtoonPageApiResponse.getWebtoonApiResponses().isEmpty()) {
				log.warn("응답이 없습니다. - Provider: {}, Page: {}", provider, page);
				break;
			}

			List<Webtoon> newWebtoons = webtoonPageApiResponse.getWebtoonApiResponses()
				.stream()
				.filter(dto -> {
					String webtoonKey = generateWebtoonKey(dto.getTitle(), provider, formatAuthors(dto.getAuthors()));
					boolean isDuplicate = existingWebtoonKeys.contains(webtoonKey);
					return !isDuplicate;
				})
				.map(WebtoonApiResponseMapper::toEntity)
				.collect(Collectors.toList());

			if (!newWebtoons.isEmpty()) {
				webtoonRepository.saveAll(newWebtoons);
				log.info("새로운 웹툰 {}개 추가 완료 - Provider: {}", newWebtoons.size(), provider);
			} else {
				log.info("Provider: {} - 추가할 새로운 웹툰이 없습니다.", provider);
			}

			isLastPage = webtoonPageApiResponse.isLastPage();
			page++;
		} while (!isLastPage);
	}

	private String generateWebtoonKey(Webtoon webtoon) {
		return webtoon.getWebtoonName() + "|" + webtoon.getPlatform().name() + "|" + webtoon.getAuthors();
	}

	private String generateWebtoonKey(String title, Platform platform, String authors) {
		return title + "|" + platform.name() + "|" + authors;
	}

	public Webtoon findWebtoon(Long id) {
		return webtoonRepository.findById(id)
			.orElseThrow(() -> new BusinessException(ErrorCode.WEBTOON_NOT_FOUND));
	}

	public Page<Webtoon> searchWebtoons(String webtoonName, Platform platform, String authors, Boolean finished,
		int page, int size, String sortBy, String sortDirection) {
		Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
		String sortField = WebtoonSort.fromString(sortBy)
			.map(WebtoonSort::getField)
			.orElse(WebtoonSort.WEBTOON_NAME.getField()); // 기본 정렬: WEBTOON_NAME

		Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

		return webtoonRepository.searchWebtoons(webtoonName, platform, authors, finished, pageable);
	}
}
