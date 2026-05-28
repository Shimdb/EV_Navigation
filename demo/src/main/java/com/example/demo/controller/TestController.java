package com.example.demo.controller;

import com.example.demo.service.EvStationSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final EvStationSyncService syncService;

    @GetMapping("/api/sync")
    public String syncData() {
        // 이 주소로 접속하면 서비스의 메서드가 실행됩니다.
        syncService.syncChargingStations();
        return "동기화 시작! 콘솔 창을 확인하세요.";
    }
}