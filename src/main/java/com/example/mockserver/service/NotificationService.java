package com.example.mockserver.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 메신저 (예: Slack)로 알림을 보내는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final RestTemplate restTemplate;

    @Value("${notification.slack.webhook-url:}")
    private String slackWebhookUrl;

    public void send(String message) {
        if (slackWebhookUrl == null || slackWebhookUrl.isBlank() || slackWebhookUrl.contains("YOUR/SLACK/WEBHOOK_URL")) {
            logger.warn("Slack Webhook URL이 설정되지 않아 알림을 보내지 않습니다.");
            return;
        }

        try {
            // Slack이 기대하는 JSON 포맷에 맞춰 페이로드를 생성합니다.
            Map<String, String> payload = Map.of("text", message);
            restTemplate.postForEntity(slackWebhookUrl, payload, String.class);
            logger.info("Slack 알림 전송 성공: {}", message);
        } catch (Exception e) {
            logger.error("Slack 알림 전송 중 에러 발생", e);
        }
    }
}
