package com.example.dbquerymybatis.model;

import java.util.List;

public record QueryResult<T>(int count, List<T> rows) {
}
