package com.example.mybatisconsumer;

import com.example.apicommon.ApiPaths;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(ApiPaths.API)
public class HealthController {

    @GetMapping(ApiPaths.HEALTH)
    public Map<String, String> health() {
        return Map.of("status", "UP", "source", "api-common");
    }
}
