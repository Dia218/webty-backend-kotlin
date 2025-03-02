package org.team14.webty.userActivity.service

import co.elastic.clients.elasticsearch.ElasticsearchClient
import org.springframework.stereotype.Service
import org.team14.webty.userActivity.document.UserActivity
import org.team14.webty.userActivity.dto.UserActivityRequest
import org.team14.webty.userActivity.repository.UserActivityRepository


@Service
class UserActivityService(
    private val userActivityRepository: UserActivityRepository,
    private val elasticsearchClient: ElasticsearchClient
) {

    fun saveActivity(request: UserActivityRequest) {
        val activity = UserActivity(
            userId = request.userId,
            webtoonId = request.webtoonId,
            webtoonName = request.webtoonName
        )

        userActivityRepository.save(activity) // Elasticsearch에 저장
    }
}