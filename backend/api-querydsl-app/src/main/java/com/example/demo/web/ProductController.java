package com.example.demo.web;

import com.example.demo.domain.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Product", description = "QueryDSL. 컨트롤러는 서비스 결과만 반환. 포장은 Advice")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "목록", description = "서비스 List 를 그대로 반환 → Advice 가 data 배열로 포장")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = ProductListEnvelope.class))
    )
    @GetMapping
    public List<ProductResponse> list() {
        return productService.list();
    }

    @Operation(summary = "단건", description = "없으면 BUSINESS_ERROR + PRODUCT_NOT_FOUND (HTTP 404)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = ProductOneEnvelope.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            content = @Content(schema = @Schema(implementation = ErrorEnvelope.class))
    )
    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        return productService.get(id);
    }

    @Operation(summary = "생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @Operation(summary = "서버 오류 샘플")
    @GetMapping("/demo/server-error")
    public ProductResponse serverError() {
        throw new IllegalStateException("boom");
    }

    @Schema(name = "ProductListEnvelope")
    public static class ProductListEnvelope extends ApiResponse<List<ProductResponse>> {
    }

    @Schema(name = "ProductOneEnvelope")
    public static class ProductOneEnvelope extends ApiResponse<ProductResponse> {
    }

    @Schema(name = "ErrorEnvelope")
    public static class ErrorEnvelope extends ApiResponse<Void> {
    }
}
