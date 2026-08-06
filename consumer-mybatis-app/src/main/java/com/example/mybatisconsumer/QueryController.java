package com.example.mybatisconsumer;

import com.example.apicommon.ApiPaths;
import com.example.dbquerymybatis.MybatisDbQueryService;
import com.example.dbquerymybatis.model.QueryResult;
import com.example.dbquerymybatis.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApiPaths.API)
public class QueryController {

    private final MybatisDbQueryService mybatisDbQueryService;

    public QueryController(MybatisDbQueryService mybatisDbQueryService) {
        this.mybatisDbQueryService = mybatisDbQueryService;
    }

    @GetMapping(ApiPaths.USERS)
    public List<User> users() {
        return mybatisDbQueryService.findAllUsers();
    }

    @GetMapping(ApiPaths.USERS + "/{id}")
    public ResponseEntity<User> user(@PathVariable long id) {
        return mybatisDbQueryService.findUserById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(ApiPaths.USERS + "/count")
    public Map<String, Long> count() {
        return Map.of("count", mybatisDbQueryService.countUsers());
    }

    @GetMapping(ApiPaths.USERS + "/by-department")
    public QueryResult<User> byDepartment(
            @RequestParam(defaultValue = "Engineering") String department
    ) {
        return mybatisDbQueryService.queryUsersByDepartment(department);
    }
}
