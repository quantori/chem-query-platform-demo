package com.quantori.chem_query_platform_demo.configurations;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Encoding;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Chem Query Platform Demo API")
                        .version("1.0")
                        .description("API for SDF upload and molecule search")
                        .contact(new Contact()
                                .name("Quantori")
                        ));
    }

    @Bean
    public OperationCustomizer customizeSearchMetadata() {
        return (operation, handlerMethod) -> {
            if (!handlerMethod.getMethod().getName().equals("search")) {
                return operation;
            }

            RequestBody rb = operation.getRequestBody();
            if (rb == null) {
                return operation;
            }

            MediaType multipart = rb.getContent().get("multipart/form-data");
            if (multipart == null) {
                return operation;
            }

            Map<String, Encoding> encoding = multipart.getEncoding();
            if (encoding == null) {
                encoding = new LinkedHashMap<>();
            }
            encoding.put("metadata",
                    new Encoding()
                            .contentType("application/json")
            );
            multipart.setEncoding(encoding);

            Schema<?> schema = multipart.getSchema();
            schema.addProperty("metadata",
                    new StringSchema()
                            .format("binary")
                            .description("JSON metadata file")
            );

            return operation;
        };
    }
}