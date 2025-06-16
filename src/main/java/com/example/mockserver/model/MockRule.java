package com.example.mockserver.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Mock 서버의 동작을 정의하는 규칙(Rule) 클래스입니다.
 * 이 규칙을 기반으로 들어온 요청을 어떻게 처리할지 결정합니다.
 */
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MockRule {

    private String ruleName; // 규칙을 식별하기 위한 이름

    // --- 조건부 (Conditions) ---
    // 이 조건들이 모두 만족되어야 규칙이 적용됩니다.
    private String userName; // 특정 사용자 이름과 일치
    private String userRegistrationNumberStartsWith; // 특정 문자열로 시작하는 주민등록번호

    // --- 실행부 (Actions) ---
    // 조건 만족 시 수행할 동작을 정의합니다.
    private ResponseSpec response; // 생성할 응답 명세

    // --- 메타데이터 (Metadata) ---
    private boolean async; // true일 경우, 비동기(webhook)로 응답
    private long delayMs; // 비동기 응답 시 지연 시간 (밀리초)

    /**
     * 이 규칙이 주어진 요청에 적용되어야 하는지 판단합니다.
     * @param request 들어온 한도 조회 요청
     * @return 규칙 적용 여부
     */
    public boolean matches(com.example.mockserver.dto.LoanDtos.LimitRequest request) {
        // Predicate를 사용하여 각 조건의 만족 여부를 체크합니다.
        Predicate<com.example.mockserver.dto.LoanDtos.LimitRequest> p = r -> true;

        if (userName != null && !userName.isBlank()) {
            p = p.and(r -> userName.equals(r.getUserName()));
        }
        if (userRegistrationNumberStartsWith != null && !userRegistrationNumberStartsWith.isBlank()) {
            p = p.and(r -> r.getUserRegistrationNumber() != null && r.getUserRegistrationNumber().startsWith(userRegistrationNumberStartsWith));
        }
        return p.test(request);
    }

    /**
     * 생성할 응답에 대한 명세를 담는 내부 클래스
     */
    @Getter
    @Setter
    @ToString
    public static class ResponseSpec {
        private Long limitAmount;
        private Double interestRate;
        private Map<String, String> customHeaders; // 응답에 추가할 커스텀 헤더
        private int httpStatus; // 반환할 HTTP 상태 코드
    }
}
