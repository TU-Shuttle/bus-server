package com.tukorea.bus.common.config;

import com.tukorea.bus.common.response.dto.ErrorResponse;
import com.tukorea.bus.common.response.dto.SuccessResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    // http://localhost:8080/swagger-ui/index.html
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(components())
                .info(info());
    }

    private Components components() {
        Components components = new Components();

        registerSchema(components, SuccessResponse.class);
        registerSchema(components, ErrorResponse.class);

        registerResponse(components);

        components.addSecuritySchemes("BearerAuth",
                new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER)
                        .name("Authorization"));

        return components;
    }

    private void registerSchema(Components components, Class<?> clazz) {
        ModelConverters.getInstance()
                .read(clazz)
                .forEach(components::addSchemas);
    }

    private void registerResponse(Components components) {
        components.addResponses("Created", buildApiResponse("Created", ResponseType.SUCCESS));
        components.addResponses("Ok", buildApiResponse("Ok", ResponseType.SUCCESS));
        components.addResponses("BadRequest", buildApiResponse("Bad Request", ResponseType.ERROR));
        components.addResponses("Unauthorized", buildApiResponse("Unauthorized", ResponseType.ERROR));
        components.addResponses("InternalServerError", buildApiResponse("Internal Server Error", ResponseType.ERROR));
    }

    private ApiResponse buildApiResponse(String description, ResponseType type) {
        Schema<?> schema = new Schema<>().$ref(type.getSchemaRef());
        MediaType mediaType = new MediaType().schema(schema);
        Content content = new Content().addMediaType("application/json", mediaType);

        return new ApiResponse().description(description).content(content);
    }

    private Info info() {
        return new Info()
                .title("RESTful API")
                .description("""
                        **RESTful API reference for developers**
                        
                        ---
                        data 필드의 정확한 구조는 **Try it out**을 통해서 확인할 수 있습니다.
                        """)
                .version("1.0");
    }

    @AllArgsConstructor
    private enum ResponseType {

        SUCCESS(SuccessResponse.class),
        ERROR(ErrorResponse.class);

        private final Class<?> responseClass;

        public String getSchemaRef() {
            return "#/components/schemas/" + responseClass.getSimpleName();
        }
    }
}
