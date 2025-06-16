package com.example.mockserver.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * API 통신에 사용되는 데이터 전송 객체(DTO)들을 포함하는 컨테이너 클래스입니다.
 */
public class LoanDtos {

    /**
     * 한도 조회 요청 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LimitRequest {
        private String userName;
        private String userRegistrationNumber; // 예: 880101-1234567
        private String webhookUrl; // 비동기 응답을 받을 URL (선택 사항)
    }

    /**
     * 한도 조회 응답 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL) // null이 아닌 필드만 JSON에 포함
    public static class LimitResponse {
        private String applicationId;
        private Long limitAmount;
        private Double interestRate;
        private String status; // 비동기 응답 시 상태 (예: "PROCESSING")
        private Map<String, String> customHeaders; // 응답에 추가할 커스텀 헤더
    }

    // ... 기존 ExecutionRequest, ExecutionResponse ...

    /**
     * 대출 실행 요청 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutionRequest {
        private String applicationId;
        private Long loanAmount;
    }

    /**
     * 대출 실행 응답 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutionResponse {
        private String status;
        private String message;
    }

    /**
     * API 에러 응답 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorResponse {
        private String errorCode;
        private String errorMessage;
    }
}
