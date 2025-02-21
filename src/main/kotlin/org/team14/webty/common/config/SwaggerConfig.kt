package org.team14.webty.common.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig(
    @Value("\${api.title:API TITLE}") private val apiTitle: String,
    @Value("\${api.description:DESCRIPTION}") private val apiDescription: String,
    @Value("\${api.version:0.0.1}") private val apiVersion: String,
) {
    @Bean
    fun api(): OpenAPI {
        return OpenAPI()
            .addSecurityItem(SecurityRequirement().addList("Bearer Authentication"))
            .components(Components().addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()))
            .info(apiInfo())
    }

    private fun apiInfo(): Info { // Swagger 에 뜨는 정보
        return Info()
            .title(apiTitle)
            .description(apiDescription)
            .version(apiVersion)
    }

    private fun createAPIKeyScheme(): SecurityScheme { // 보안
        return SecurityScheme().type(SecurityScheme.Type.HTTP) // 스키마 유형 HTTP
            .bearerFormat("JWT") // 토큰 형식
            .scheme("bearer") // 스키마 이름
    }
}