package com.example.consumer;

import com.example.dbquery.DbQueryService;
import com.example.dbquery.model.QueryResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class QueryController {

    private final DbQueryService dbQueryService;

    public QueryController(DbQueryService dbQueryService) {
        this.dbQueryService = dbQueryService;
    }

    @GetMapping("/users")
    public List<Map<String, Object>> users() {
        return dbQueryService.findAll("users");
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> user(@PathVariable long id) {
        return dbQueryService.findById("users", "id", id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/users/count")
    public Map<String, Long> count() {
        return Map.of("count", dbQueryService.count("users"));
    }

    @GetMapping("/query")
    public QueryResult query(
            @RequestParam(defaultValue = "SELECT id, name, email, department FROM users ORDER BY id") String sql
    ) {
        return dbQueryService.queryAsResult(sql);
    }
}
