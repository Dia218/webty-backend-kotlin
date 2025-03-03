package org.team14.webty.userActivity.service

import co.elastic.clients.elasticsearch.ElasticsearchClient
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.query.StringQuery
import org.springframework.stereotype.Service
import org.team14.webty.userActivity.document.UserActivity
import org.team14.webty.userActivity.dto.UserActivityRequest
import org.team14.webty.userActivity.repository.UserActivityRepository


@Service
class UserActivityService(
    private val userActivityRepository: UserActivityRepository,
    private val elasticsearchClient: ElasticsearchClient,
    private val elasticsearchTemplate: ElasticsearchTemplate,
    //private val boolQueryBuilder: BoolQueryBuilder
) {

    //사용자 로그 저장(웹툰+유저)
    fun saveActivity(request: UserActivityRequest) {
        val activity = UserActivity(
            userId = request.userId,
            webtoonId = request.webtoonId,
            webtoonName = request.webtoonName
        )

        userActivityRepository.save(activity) // Elasticsearch에 저장
    }

    //유저가 최근에 조회한 웹툰 3개 조회(최신순)
    fun getRecentWebtoons(userId: String, limit: Int): List<String> {
        //유저가 조회한 최근 웹툰 모두 가져오기
        val allActivities = userActivityRepository.findByUserIdOrderByDateDesc(userId)
        //TODO: 모든 웹툰 가져오지 말기.

        //웹툰 ID 중복 제거하면서 최신 순으로 3개 선택
        return allActivities
            .distinctBy { it.webtoonId } // webtoonId 기준 중복 제거
            .take(limit) // 3개만 선택
            .map { it.webtoonId } // 웹툰 ID만 추출하여 반환
    }

    //매개변수 : 유저가 최근에 조회한 웹툰들 id 리스트
    //반환 값 : 유저와 같은 웹툰을 본 유저들 id 리스트
    fun getSimilarUsers(webtoonIds: List<String>): List<String> {
        val results = userActivityRepository.findByWebtoonIdIn(webtoonIds)
        return results.map { it.userId }.distinct() // 중복 제거 후 유저 ID 반환
    }

    //매개변수 : 유저와 유사한 유저들 id 리스트
    //반환 값 : 그 유저들이 많이 본 웹툰 리스트
    fun getRecommendWebtoons(userIds: List<String>):List<String> {
        if (userIds.isEmpty()) return emptyList() // 유사한 유저가 없으면 빈 리스트 반환

        // 해당 유저들이 조회한 웹툰 가져오기
        val activities = userActivityRepository.findByUserIdIn(userIds)

        // 웹툰 조회 횟수 집계
        val webtoonFrequency = activities
            .map { it.webtoonId } // 웹툰 ID만 추출
            .groupingBy { it } // 웹툰 ID별 그룹화
            .eachCount() // 조회 횟수 계산

        // 많이 본 웹툰 TOP 5 반환
        return webtoonFrequency.entries
            .sortedByDescending { it.value } // 조회 횟수 내림차순 정렬
            .take(5) // 최대 5개 선택
            .map { it.key }
    }

}