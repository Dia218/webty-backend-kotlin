package org.team14.webty.common.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.team14.webty.common.interceptor.WebSocketAuthInterceptor

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {
    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/topic") // 클라이언트 구독 엔드포인트
        registry.setApplicationDestinationPrefixes("/app") // 클라이언트가 메시지 보낼때 사용
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/websocket") // 웹소켓 엔드포인트
            .setAllowedOriginPatterns("*")
            .addInterceptors(WebSocketAuthInterceptor())
            .withSockJS()
    }
}
