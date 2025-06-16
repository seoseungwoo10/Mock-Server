package com.example.mockserver.service;

import com.example.mockserver.model.MockRule;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Mock 규칙(Rule)을 관리하는 서비스입니다.
 * 규칙은 메모리에 저장됩니다.
 */
@Service
public class MockConfigService {

    // 동시성 환경에서 안전하게 리스트를 사용하기 위해 CopyOnWriteArrayList를 사용합니다.
    private final List<MockRule> rules = new CopyOnWriteArrayList<>();

    public MockConfigService() {
        // 애플리케이션 시작 시 기본 규칙 추가
        MockRule defaultRuleA = new MockRule();
        defaultRuleA.setRuleName("Default-Male-User");
        defaultRuleA.setUserRegistrationNumberStartsWith("880101-1");
        MockRule.ResponseSpec responseA = new MockRule.ResponseSpec();
        responseA.setLimitAmount(50_000_000L);
        responseA.setInterestRate(5.5);
        responseA.setHttpStatus(200);
        defaultRuleA.setResponse(responseA);
        rules.add(defaultRuleA);

        MockRule defaultRuleB = new MockRule();
        defaultRuleB.setRuleName("Default-Female-User-Async");
        defaultRuleB.setUserRegistrationNumberStartsWith("950202-2");
        MockRule.ResponseSpec responseB = new MockRule.ResponseSpec();
        responseB.setLimitAmount(100_000_000L);
        responseB.setInterestRate(4.5);
        responseB.setHttpStatus(200);
        defaultRuleB.setResponse(responseB);
        defaultRuleB.setAsync(true); // 비동기 응답 규칙
        defaultRuleB.setDelayMs(3000); // 3초 지연
        rules.add(defaultRuleB);
    }

    public void addRule(MockRule rule) {
        rules.add(0, rule); // 새로운 규칙을 항상 최우선으로 적용
    }

    public List<MockRule> getRules() {
        return rules;
    }

    public Optional<MockRule> findMatchingRule(com.example.mockserver.dto.LoanDtos.LimitRequest request) {
        return rules.stream()
                .filter(rule -> rule.matches(request))
                .findFirst();
    }
}
