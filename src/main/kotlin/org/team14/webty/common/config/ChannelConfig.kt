package org.team14.webty.common.config

import kotlinx.coroutines.channels.Channel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ChannelConfig {

    @Bean
    fun requestChannel(): Channel<Unit> = Channel(Channel.UNLIMITED)
}