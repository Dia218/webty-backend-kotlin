package org.team14.webty.userActivity.repository

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Repository
import org.team14.webty.userActivity.document.UserActivity

@Repository
interface UserActivityRepository : ElasticsearchRepository<UserActivity, String>
