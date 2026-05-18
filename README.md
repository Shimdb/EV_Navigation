# 🚗 EV NAV (전기차 내비게이션)

> **공공데이터와 공간 정보(Spatial Data)를 활용한 전기차 충전소 검색 및 길 안내 서비스**
> 
> 캡스톤 디자인 프로젝트 - 대전대학교 컴퓨터공학과

<br>

## 📌 프로젝트 소개
EV NAV는 전기차 운전자들의 가장 큰 고민거리인 **'충전소 탐색 및 경로 안내'**를 돕기 위해 기획된 서비스입니다. 
한국환경공단(공공데이터포털)의 실시간 충전소 API를 연동하여 전국 충전소 데이터를 구축하고, 사용자의 현재 위치를 기반으로 최적의 충전소를 빠르게 찾아줍니다.

<br>

## 🛠 기술 스택 (Tech Stack)

### Backend
*   **Language:** 
*   **Framework:** 
*   **Build Tool:**

### Frontend
*   **Language:** Kotlin
*   **Map API:** [Kakao Map API / T Map API]

### database: MySQL 

### API & Tools
*   **Open API:** 공공데이터포털 (한국환경공단_전기자동차 충전소 정보)
*   **IDE:** Java eclipse
*   **Version Control:** Git & GitHub

<br>

## ✨ 핵심 기능 (Key Features)

### 1. 🔄 공공데이터 대용량 동기화 (Bulk Insert)
*   Spring Scheduler(`@Scheduled`)를 활용하여 매일 새벽 3시에 전국 충전소 최신 데이터를 자동 동기화합니다.
*   수만 건의 데이터를 효율적으로 저장하기 위해 Spring JDBC의 `BatchPreparedStatementSetter`를 활용한 **Bulk Insert** 기법을 적용하여 DB 저장 속도를 극적으로 개선했습니다.

### 2. 📍 공간 데이터(Spatial Data) 기반 위치 검색
*   위도(Lat)와 경도(Lng) 데이터를 단순 숫자가 아닌 MySQL의 공간 데이터 타입(`POINT`)으로 변환하여 저장(`ST_GeomFromText`)했습니다.
*   이를 통해 향후 반경 검색(예: 내 주변 3km 이내 충전소) 시, 애플리케이션 레벨의 복잡한 연산 없이 데이터베이스 레벨에서 초고속 거리 계산이 가능하도록 설계했습니다.


