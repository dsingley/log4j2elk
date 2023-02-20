package com.dsingley.log4j2elk;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

import java.util.Map;

@Builder
@Getter
public class ElkConfiguration {
    String baseUrl;
    String indexName;
    @Singular Map<String, String> additionalFields;
}
