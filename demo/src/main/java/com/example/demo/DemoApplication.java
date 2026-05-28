package com.example.demo; // 💡 맨 윗줄 본인의 패키지 이름은 그대로 유지하세요!

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // 💡 추가됨

@EnableScheduling // 💡 핵심! 스프링 부트에게 "스케줄러(자동 실행) 기능 켜줘!" 라고 알려주는 스위치
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}