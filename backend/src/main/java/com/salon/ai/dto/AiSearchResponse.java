package com.salon.ai.dto;

import java.util.List;

public record AiSearchResponse(String query, List<AiSearchResult> results) {}
