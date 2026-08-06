package com.example.dbquery.model;

import java.util.List;
import java.util.Map;

public record QueryResult(int count, List<Map<String, Object>> rows) {
}
