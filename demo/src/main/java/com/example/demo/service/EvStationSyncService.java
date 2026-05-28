package com.example.demo.service;

import com.example.demo.dto.EvStationDto;
import com.example.demo.repository.EvStationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvStationSyncService { // 💡 핵심: 클래스 선언이 추가되었습니다!

    // 의존성 주입을 받을 객체들 (선언 필수)
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final EvStationRepository repository;

    // API 관련 변수 선언 (application.yml에서 값을 가져오거나 직접 문자열 할당 필요)
    @Value("http://apis.data.go.kr/B552584/EvCharger")
    private String baseUrl;

    @Value("ccf86abf48f0decbfd5f0c0eb14764a6fd981bbb4a3ebc71ff33c74bbe50fcd5")
    private String serviceKey;

    public void syncChargingStations() {
        int pageNo = 1;
        int numOfRows = 2000;
        boolean hasMoreData = true;

        while (hasMoreData) {
            try {
                // 1. API 주소 생성
                String urlString = String.format("%s?serviceKey=%s&pageNo=%d&numOfRows=%d&dataType=JSON",
                        baseUrl, serviceKey, pageNo, numOfRows);

                // 2. API 호출 및 JSON 받기 (URISyntaxException 방지를 위해 try-catch 내부로 이동)
                URI uri = new URI(urlString);
                String jsonResponse = restTemplate.getForObject(uri, String.class);

                // 3. JSON을 자바 객체(DTO)로 변환
                EvStationDto.ApiResponse response = objectMapper.readValue(jsonResponse, EvStationDto.ApiResponse.class);
                List<EvStationDto.Item> items = response.getBody().getItems().getItem();

                if (items == null || items.isEmpty()) {
                    hasMoreData = false; // 더 이상 데이터가 없으면 종료
                } else {
                    // 4. 리포지토리를 통해 DB에 벌크 저장
                    repository.bulkInsert(items);
                    pageNo++;
                }
            } catch (Exception e) {
                // URISyntaxException, JsonProcessingException 등 예외 발생 시 로그 출력 후 루프 종료
                log.error("충전소 데이터 동기화 중 오류 발생 (pageNo: {}): ", pageNo, e);
                hasMoreData = false;
            }
        }
    }
}