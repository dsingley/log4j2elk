package com.dsingley.log4j2elk;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

import java.util.Map;

@Builder
@Getter
public class ElkConfiguration {
    @Builder.Default boolean enabled = true;
    @NonNull String baseUrl;
    @NonNull String indexName;
    @Singular Map<String, String> additionalFields;

    public String getValue(String key) {
        return additionalFields.entrySet().stream()
                .filter(entry -> entry.getKey().equals(key))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}
