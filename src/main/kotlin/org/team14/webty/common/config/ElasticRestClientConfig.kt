package org.team14.webty.common.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration


@Configuration
class ElasticRestClientConfig : ElasticsearchConfiguration() {

    @Value("\${spring.elasticsearch.uris}")
    private lateinit var uris: String

    override fun clientConfiguration(): ClientConfiguration {
        val formattedUris = uris.replace("http://", "").replace("https://", "") // 프로토콜 제거

        return ClientConfiguration.builder()
            .connectedTo(formattedUris)
            .build()
    }
}