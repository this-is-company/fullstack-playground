package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("api-querydsl-app")
                .description("QueryDSL 조회 + 공통 응답 { status, data, error }")
                .version("1.0.0"));
    }

    @Bean
    OpenApiCustomizer schemaDocs() {
        return openApi -> {
            annotateSchemaUsage(openApi);
            sortSchemas(openApi);
        };
    }

    /** 하단 Schemas 설명에 이 모델이 쓰이는 API 를 붙인다. */
    static void annotateSchemaUsage(OpenAPI openApi) {
        if (openApi.getPaths() == null || openApi.getComponents() == null
                || openApi.getComponents().getSchemas() == null) {
            return;
        }
        Map<String, Set<String>> usedBy = new LinkedHashMap<>();
        openApi.getPaths().forEach((path, item) ->
                item.readOperationsMap().forEach((method, operation) -> {
                    String label = method.name() + " " + path;
                    collectFromOperation(operation, usedBy, label);
                }));
        propagateNestedRefs(openApi, usedBy);
        usedBy.forEach((name, apis) -> {
            Schema<?> schema = openApi.getComponents().getSchemas().get(name);
            if (schema == null || apis.isEmpty()) {
                return;
            }
            String line = "사용 API: " + String.join(", ", apis);
            String desc = schema.getDescription();
            if (desc == null || desc.isBlank()) {
                schema.setDescription(line);
            } else if (!desc.contains("사용 API:")) {
                schema.setDescription(desc + "\n\n" + line);
            }
        });
    }

    private static void collectFromOperation(
            Operation operation,
            Map<String, Set<String>> usedBy,
            String label
    ) {
        collectFromRequest(operation.getRequestBody(), usedBy, label);
        if (operation.getResponses() == null) {
            return;
        }
        operation.getResponses().forEach((code, response) ->
                collectFromContent(response.getContent(), usedBy, label + " (" + code + ")"));
    }

    private static void collectFromRequest(
            RequestBody body,
            Map<String, Set<String>> usedBy,
            String label
    ) {
        if (body != null) {
            collectFromContent(body.getContent(), usedBy, label);
        }
    }

    private static void collectFromContent(Content content, Map<String, Set<String>> usedBy, String label) {
        if (content == null) {
            return;
        }
        content.values().forEach(media -> addSchemaRefs(media.getSchema(), usedBy, label));
    }

    private static void addSchemaRefs(Schema<?> schema, Map<String, Set<String>> usedBy, String label) {
        walkSchemaRefs(schema, name ->
                usedBy.computeIfAbsent(name, key -> new LinkedHashSet<>()).add(label));
    }

    private static void propagateNestedRefs(OpenAPI openApi, Map<String, Set<String>> usedBy) {
        Map<String, Schema> schemas = openApi.getComponents().getSchemas();
        for (String parent : new ArrayList<>(usedBy.keySet())) {
            Schema<?> schema = schemas.get(parent);
            if (schema == null) {
                continue;
            }
            Set<String> apis = usedBy.get(parent);
            walkSchemaRefs(schema, nested -> {
                if (!nested.equals(parent)) {
                    usedBy.computeIfAbsent(nested, key -> new LinkedHashSet<>()).addAll(apis);
                }
            });
        }
    }

    private static void walkSchemaRefs(Schema<?> schema, Consumer<String> onName) {
        if (schema == null) {
            return;
        }
        if (schema.get$ref() != null) {
            onName.accept(refName(schema.get$ref()));
        }
        if (schema.getItems() != null) {
            walkSchemaRefs(schema.getItems(), onName);
        }
        if (schema.getProperties() != null) {
            schema.getProperties().values().forEach(prop -> walkSchemaRefs((Schema<?>) prop, onName));
        }
        if (schema.getAllOf() != null) {
            schema.getAllOf().forEach(inner -> walkSchemaRefs(inner, onName));
        }
        if (schema.getAnyOf() != null) {
            schema.getAnyOf().forEach(inner -> walkSchemaRefs(inner, onName));
        }
        if (schema.getOneOf() != null) {
            schema.getOneOf().forEach(inner -> walkSchemaRefs(inner, onName));
        }
        if (schema.getAdditionalProperties() instanceof Schema<?> extra) {
            walkSchemaRefs(extra, onName);
        }
    }

    private static String refName(String ref) {
        return ref.substring(ref.lastIndexOf('/') + 1);
    }

    static void sortSchemas(OpenAPI openApi) {
        Map<String, Schema> schemas = openApi.getComponents().getSchemas();
        if (schemas == null || schemas.isEmpty()) {
            return;
        }
        Map<String, Schema> ordered = new LinkedHashMap<>();
        schemas.entrySet().stream()
                .sorted(Comparator
                        .comparing((Map.Entry<String, Schema> e) -> groupName(e.getKey()))
                        .thenComparingInt(e -> kind(e.getKey())))
                .forEach(e -> ordered.put(e.getKey(), e.getValue()));
        openApi.getComponents().setSchemas(ordered);
    }

    private static String groupName(String name) {
        return name.replace("Request", "")
                .replace("Response", "")
                .trim()
                .toLowerCase();
    }

    private static int kind(String name) {
        if (name.endsWith("Request")) {
            return 1;
        }
        if (name.endsWith("Response")) {
            return 2;
        }
        return 3;
    }
}
