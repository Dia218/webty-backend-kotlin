package org.team14.webty.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {
	@Value("${api.title:API TITLE}")
	private String apiTitle;

	@Value("${api.description:DESCRIPTION}")
	private String apiDescription;

	@Value("${api.version:0.0.1}")
	private String apiVersion;

	@Bean
	public OpenAPI api() {

		return new OpenAPI()
			.addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
			.components(new Components().addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()))
			.info(apiInfo());
	}

	private Info apiInfo() { // Swagger 에 뜨는 정보
		return new Info()
			.title(apiTitle)
			.description(apiDescription)
			.version(apiVersion);
	}

	private SecurityScheme createAPIKeyScheme() { // 보안
		return new SecurityScheme().type(SecurityScheme.Type.HTTP) // 스키마 유형 HTTP
			.bearerFormat("JWT") // 토큰 형식
			.scheme("bearer"); // 스키마 이름
	}

}