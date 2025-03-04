package org.team14.webty.userActivity.repository

import org.springframework.data.elasticsearch.annotations.Query
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Repository
import org.team14.webty.userActivity.document.UserActivity

@Repository
interface UserActivityRepository : ElasticsearchRepository<UserActivity, String>{
    fun findByUserIdOrderByDateDesc(userId: String): List<UserActivity>

    //웹툰id 리스트의 userActivity 가져오기 / In은 웹툰id가 여러개란 뜻
    fun findByWebtoonIdIn(webtoonIds: List<String>): List<UserActivity>

    // 특정 유저 리스트의 userActivity 가져오기
    fun findByUserIdIn(userIds: List<String>): List<UserActivity>
}
