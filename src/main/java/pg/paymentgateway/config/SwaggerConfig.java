package pg.paymentgateway.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jdk.javadoc.doclet.Doclet;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(title = "Payment-Gateway API 명세서",
                    description = "결제 대행 서비스 API 명세서",
                    version = "v1"
        ))
@RequiredArgsConstructor
@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi groupedOpenApi() {
        String[] paths = {"/api/v1/**"};

        return GroupedOpenApi.builder()
                .group("결제대행서비스 API v1")
                .pathsToMatch(paths)
                .build();
    }

    @Bean
    public OpenAPI openAPI() {
        // SecuritySecheme명
        String authorization = "authorization";
        // API 요청헤더에 인증정보 포함
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(authorization);
        // SecuritySchemes 등록
        Components components = new Components()
                .addSecuritySchemes(authorization, new SecurityScheme()
                        .name(authorization)
                        .type(SecurityScheme.Type.HTTP) // HTTP 방식
                        .scheme("bearer")
                        .bearerFormat("Authorization")); // 토큰 형식을 지정하는 임의의 문자(Optional)

        return new OpenAPI()
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}
