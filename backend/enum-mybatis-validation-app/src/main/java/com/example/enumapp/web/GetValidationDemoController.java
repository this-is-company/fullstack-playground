package com.example.enumapp.web;

import com.example.enumapp.common.time.DateStrings;
import com.example.enumapp.web.dto.GetValidationParsedResponse;
import com.example.enumapp.web.dto.GetValidationQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.enumapp.web.validation.NotEmptyList;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * GET 쿼리 파라미터 검증 데모.
 * <p>
 * 방법 A: 문자열 날짜 + {@code @LocalDateString} 검증 → {@link DateStrings} 로 변환<br>
 * 방법 B: {@link LocalDate}/{@link LocalDateTime} 파라미터 + {@code @DateTimeFormat} (Spring 바인딩 변환)
 */
@Tag(name = "GET Validation Demo", description = "GET 리스트/중첩/숫자/날짜 검증 및 LocalDate 변환 확인")
@RestController
@RequestMapping("/api/demo/get-validation")
@Validated
public class GetValidationDemoController {

    @Operation(
            summary = "문자열 날짜 검증 후 LocalDate/LocalDateTime 변환",
            description = """
                    ids·items 리스트(null/empty/요소 null/내부 필드) + 숫자 + 날짜 문자열을 검증한다.
                    통과 후 DateStrings 로 LocalDate/LocalDateTime 으로 바꾼 결과를 반환한다.
                    예: ?ids=1&ids=2&items[0].sku=ABC&items[0].quantity=2&orderDate=2026-08-06&minQuantity=5
                    """
    )
    @GetMapping("/search")
    public GetValidationParsedResponse search(@Valid @ModelAttribute GetValidationQuery query) {
        GetValidationParsedResponse response = new GetValidationParsedResponse();
        response.setIds(query.getIds());
        response.setItems(query.getItems().stream()
                .map(i -> new GetValidationParsedResponse.ItemView(i.getSku(), i.getQuantity()))
                .toList());
        response.setMinQuantity(query.getMinQuantity());
        response.setPage(query.getPage());
        response.setOrderDate(DateStrings.toLocalDate(query.getOrderDate()));
        response.setFromDate(DateStrings.toLocalDate(query.getFromDate()));
        response.setToDate(DateStrings.toLocalDate(query.getToDate()));
        response.setFromDateTime(DateStrings.toLocalDateTime(query.getFromDateTime()));
        response.setToDateTime(DateStrings.toLocalDateTime(query.getToDateTime()));
        response.setNote("validated as strings then converted via DateStrings");
        return response;
    }

    @Operation(
            summary = "Spring @DateTimeFormat 으로 바로 LocalDate/LocalDateTime 바인딩",
            description = """
                    쿼리 문자열을 Spring 이 LocalDate/LocalDateTime 으로 변환한다.
                    형식이 틀리면 바인딩 오류(400). null/생략은 허용.
                    예: ?orderDate=2026-08-06&fromDateTime=2026-08-06T09:00:00&minQuantity=3
                    """
    )
    @GetMapping("/typed-dates")
    public Map<String, Object> typedDates(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate orderDate,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime fromDateTime,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime toDateTime,

            @RequestParam(required = false)
            @jakarta.validation.constraints.Min(0)
            Integer minQuantity
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("orderDate", orderDate);
        body.put("orderDateType", orderDate == null ? null : orderDate.getClass().getSimpleName());
        body.put("fromDateTime", fromDateTime);
        body.put("fromDateTimeType", fromDateTime == null ? null : fromDateTime.getClass().getSimpleName());
        body.put("toDateTime", toDateTime);
        body.put("minQuantity", minQuantity);
        body.put("note", "bound directly to LocalDate/LocalDateTime by Spring");
        return body;
    }

    @Operation(
            summary = "필수 ids 리스트 GET 검증",
            description = "ids 가 없거나 비면 400. 요소는 Long 으로 바인딩."
    )
    @GetMapping("/required-ids")
    public Map<String, Object> requiredIds(
            @RequestParam
            @NotEmptyList
            List<@NotNull Long> ids
    ) {
        return Map.of(
                "ids", ids,
                "note", "List<@NotNull Long> + custom @NotEmptyList"
        );
    }
}
