package com.example.mockserver.controller;

import com.example.mockserver.dto.LoanDtos;
import com.example.mockserver.service.MockLoanService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

/**
 * 대출 관련 API 요청을 처리하는 REST 컨트롤러입니다.
 * Relay(Proxy) 기능을 포함합니다.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LoanController {

    private static final Logger logger = LoggerFactory.getLogger(LoanController.class);
    private final MockLoanService mockLoanService;
    private final RestTemplate restTemplate;

    @Value("${relay.enabled:false}")
    private boolean relayEnabled;

    @Value("${relay.target-url:}")
    private String relayTargetUrl;

    /**
     * 대출 한도 조회 API 엔드포인트입니다.
     * @param request 한도 조회 요청 DTO
     * @return 성공 시 한도 조회 결과, 실패 시 에러 응답. 규칙이 없으면 Relay 시도.
     */
    @PostMapping("/limits")
    public ResponseEntity<?> inquireLimit(@RequestBody LoanDtos.LimitRequest request, HttpServletRequest servletRequest) throws URISyntaxException {
        Optional<LoanDtos.LimitResponse> mockResponseOpt = mockLoanService.inquireLimit(request);

        if (mockResponseOpt.isPresent()) {
            LoanDtos.LimitResponse mockResponse = mockResponseOpt.get();

            // 커스텀 헤더 추가
            HttpHeaders headers = new HttpHeaders();
            if (mockResponse.getCustomHeaders() != null) {
                mockResponse.getCustomHeaders().forEach(headers::add);
            }

            // 비동기 응답인 경우 202 Accepted 반환
            if ("PROCESSING".equals(mockResponse.getStatus())) {
                return new ResponseEntity<>(mockResponse, headers, HttpStatus.ACCEPTED);
            }

            return new ResponseEntity<>(mockResponse, headers, HttpStatus.OK);
        }

        // 매칭되는 Mock 규칙이 없고, Relay 기능이 활성화된 경우
        if (relayEnabled) {
            logger.warn("매칭되는 Mock 규칙이 없어 Relay 서버로 요청을 전달합니다. Target: {}", relayTargetUrl);
            return relayRequest(servletRequest, request);
        }

        // Relay 기능도 비활성화된 경우 404 Not Found 반환
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new LoanDtos.ErrorResponse("NO_MATCHING_RULE", "요청과 일치하는 규칙을 찾을 수 없습니다."));
    }

    /**
     * 요청을 실제 서버로 전달(Relay)합니다.
     */
    private ResponseEntity<?> relayRequest(HttpServletRequest servletRequest, Object body) throws URISyntaxException {
        String requestUrl = servletRequest.getRequestURI();
        URI uri = new URI(relayTargetUrl + requestUrl);

        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = servletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            // 호스트 정보는 target URL의 것으로 대체해야 하므로 복사하지 않음
            if (!headerName.equalsIgnoreCase("host")) {
                headers.put(headerName, List.of(servletRequest.getHeader(headerName)));
            }
        }

        HttpEntity<Object> httpEntity = new HttpEntity<>(body, headers);
        HttpMethod method = HttpMethod.valueOf(servletRequest.getMethod());

        try {
            return restTemplate.exchange(uri, method, httpEntity, byte[].class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsByteArray());
        }
    }

    // ... 기존 대출 실행 및 예외 핸들러 ...
    /**
     * 대출 실행 API 엔드포인트입니다.
     */
    @PostMapping("/executions")
    public ResponseEntity<LoanDtos.ExecutionResponse> executeLoan(@RequestBody LoanDtos.ExecutionRequest request) {
        // 이 부분도 필요에 따라 규칙 기반 또는 Relay 로직으로 확장할 수 있습니다.
        // 현재는 구현에서 제외
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<LoanDtos.ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        LoanDtos.ErrorResponse errorResponse = new LoanDtos.ErrorResponse("INVALID_INPUT", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
