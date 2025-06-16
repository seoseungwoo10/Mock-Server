package com.example.mockserver.service;

import com.example.mockserver.dto.LoanDtos;
import com.example.mockserver.model.MockRule;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 새로운 규칙 기반 Mock 대출 서비스 로직입니다.
 */
@Service
@RequiredArgsConstructor
public class MockLoanService {

    private static final Logger logger = LoggerFactory.getLogger(MockLoanService.class);
    private final MockConfigService mockConfigService;
    private final NotificationService notificationService;
    private final Faker faker;
    private final RestTemplate restTemplate;

    /**
     * 한도 조회를 처리합니다.
     * @param request 한도 조회 요청
     * @return 매칭된 규칙에 따른 응답 결과. 매칭되는 규칙이 없으면 비어있는 Optional을 반환합니다.
     */
    public java.util.Optional<LoanDtos.LimitResponse> inquireLimit(LoanDtos.LimitRequest request) {
        // 요청과 매칭되는 규칙을 찾습니다.
        java.util.Optional<MockRule> matchingRuleOpt = mockConfigService.findMatchingRule(request);

        if (matchingRuleOpt.isEmpty()) {
            return java.util.Optional.empty(); // 매칭 규칙 없음 -> Relay 서버로 전달될 것임
        }

        MockRule rule = matchingRuleOpt.get();
        logger.info("요청에 대한 규칙 매칭 성공: {}", rule.getRuleName());
        notificationService.send("규칙 실행됨: " + rule.getRuleName());

        // 규칙에 따라 비동기 또는 동기 처리를 분기합니다.
        if (rule.isAsync()) {
            processAsynchronously(request, rule);
            // 비동기 처리 시에는 우선 '처리중' 상태를 즉시 응답합니다.
            LoanDtos.LimitResponse immediateResponse = new LoanDtos.LimitResponse();
            immediateResponse.setStatus("PROCESSING");
            immediateResponse.setApplicationId(UUID.randomUUID().toString());
            return java.util.Optional.of(immediateResponse);
        } else {
            // 동기 처리
            return java.util.Optional.of(createSyncResponse(rule));
        }
    }

    /**
     * 동기 응답 객체를 생성합니다.
     */
    private LoanDtos.LimitResponse createSyncResponse(MockRule rule) {
        MockRule.ResponseSpec spec = rule.getResponse();

        // Faker를 사용하여 더 현실적인 데이터 생성
        long generatedLimit = faker.number().numberBetween(spec.getLimitAmount() - 5000000, spec.getLimitAmount() + 5000000);

        return new LoanDtos.LimitResponse(
                faker.internet().uuid(),
                generatedLimit,
                spec.getInterestRate(),
                "SUCCESS",
                spec.getCustomHeaders()
        );
    }

    /**
     * 비동기 응답을 처리합니다.
     * 별도의 스레드에서 지연 시간 후 Webhook으로 결과를 전송합니다.
     */
    private void processAsynchronously(LoanDtos.LimitRequest request, MockRule rule) {
        CompletableFuture.runAsync(() -> {
            try {
                logger.info("{}ms 후 Webhook 전송 시작: {}", rule.getDelayMs(), request.getWebhookUrl());
                TimeUnit.MILLISECONDS.sleep(rule.getDelayMs());

                LoanDtos.LimitResponse finalResponse = createSyncResponse(rule);

                restTemplate.postForEntity(request.getWebhookUrl(), finalResponse, String.class);
                logger.info("Webhook 전송 성공: {}", request.getWebhookUrl());
                notificationService.send("비동기 응답 전송 완료: " + rule.getRuleName());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("비동기 처리 중 스레드 인터럽트 발생", e);
            } catch (Exception e) {
                logger.error("Webhook 전송 실패: {}", request.getWebhookUrl(), e);
                notificationService.send("비동기 응답 전송 실패: " + rule.getRuleName());
            }
        });
    }

    // ... 대출 실행 로직 (Execution)은 기존 로직을 재사용하거나 확장할 수 있습니다 ...
}
