package com.example.mockserver.controller;

import com.example.mockserver.model.MockRule;
import com.example.mockserver.service.MockConfigService;
import com.example.mockserver.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Mock 규칙을 관리하기 위한 REST 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/v1/mock-configs")
@RequiredArgsConstructor
public class MockConfigController {

    private final MockConfigService mockConfigService;
    private final NotificationService notificationService;

    /**
     * 새로운 Mock 규칙을 추가합니다.
     * @param rule 추가할 규칙 객체
     * @return 성공 메시지
     */
    @PostMapping
    public ResponseEntity<String> addRule(@RequestBody MockRule rule) {
        mockConfigService.addRule(rule);
        notificationService.send("새로운 Mock 규칙 추가됨: " + rule.getRuleName());
        return ResponseEntity.ok("규칙이 성공적으로 추가되었습니다: " + rule.getRuleName());
    }

    /**
     * 현재 설정된 모든 Mock 규칙 목록을 조회합니다.
     * @return 규칙 목록
     */
    @GetMapping
    public ResponseEntity<List<MockRule>> getRules() {
        return ResponseEntity.ok(mockConfigService.getRules());
    }
}
