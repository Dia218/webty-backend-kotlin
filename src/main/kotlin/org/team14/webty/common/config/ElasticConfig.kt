package org.team14.webty.common.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories


@Configuration
@EnableElasticsearchRepositories(basePackages = ["org.team14.webty.userActivity.repository"])
class ElasticsearchConfig