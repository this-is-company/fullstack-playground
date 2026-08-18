package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("api-jpa-app")
                .description("JPA 조회 + 공통 응답 { status, data, error }. 맨 아래 Schemas 의 「Product (Request + Response)」에서 요청/응답 필드를 같이 본다.")
                .version("1.0.0"));
    }

    @Bean
    OpenApiCustomizer productRequestResponseSchema() {
        return openApi -> {
            Schema<?> combined = new ObjectSchema()
                    .description("상품 API Request / Response. 펼치면 필드·타입이 표처럼 보인다.")
                    .addProperty("request", new Schema<>()
                            .$ref("#/components/schemas/ProductRequest")
                            .description("요청 본문 (POST /api/products)"))
                    .addProperty("response", new Schema<>()
                            .$ref("#/components/schemas/ProductResponse")
                            .description("성공 시 data 단건"))
                    .addProperty("responseList", new ArraySchema()
                            .items(new Schema<>().$ref("#/components/schemas/ProductResponse"))
                            .description("성공 시 data 목록"));
            openApi.getComponents().addSchemas("Product (Request + Response)", combined);
        };
    }
}
