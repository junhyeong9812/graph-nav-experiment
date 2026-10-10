# 프로젝트 문서 인덱스

이 저장소 개발자가 쓴 문서다. 상세 문서는 `nav/` 아래에 있다 (원문 링크의 `/docs/...` 는 `nav/...` 에 해당한다).

## 문서 목록 (`nav/` 기준)

- `./`: ACCESSLOG_README.md, ADDRESS_README.md, AUTH_README.md, BANKACCOUNT_README.md, BLOCKPROFILE_README.md, CHATQUESTION_README.md, FAQ_README.md, FOLLOW_README.md, INSPECTION_README.md, NOTICE_README.md, NOTIFICATION_README.md, ORDER_README.md, PAYMENT_README.md, POINT_README.md, PROFILE_README.md, SALE_README.md, SHIPMENT_README.md, STYLE_README.md, USER_README.md, WAREHOUSESTORAGE_README.md, WEATHER_README.md, runAsync&Kafka_README.md
- `domain/FAQ/`: README.md
- `domain/address/`: README.md
- `domain/addressLog/`: AOP_README.md, README.md
- `domain/chatQuestion/`: AOP_README.md, README.md
- `domain/event/`: AOP_README.md, README.md
- `domain/inquiry/`: README.md
- `domain/inspection/`: README.md
- `domain/notice/`: README.md
- `domain/notification/`: README.md
- `domain/order/`: README.md
- `domain/payment/`: README.md
- `domain/product/`: README.md
- `domain/product/indexSetting/`: README.md
- `domain/shipment/`: README.md
- `domain/weather/`: README.md
- `event/`: README.md
- `exception/`: springBoot_Exception.md
- `global/`: README.md
- `global/config/`: README.md
- `global/config/batch/`: README.md
- `global/config/kafka/`: README.md
- `global/config/openai/`: README.md
- `global/config/redis/`: README.md
- `global/config/web/`: README.md
- `global/config/web/logging/`: README.md
- `global/config/web/mvc/`: README.md
- `global/config/web/rest/`: README.md
- `global/config/websocket/`: README.md
- `global/dto/`: README.md
- `global/entity/`: README.md
- `global/logging/`: README.md
- `global/security/`: README.md
- `global/utils/`: README.md
- `global/websocket/`: README.md
- `portfolio/`: ELASTICSEARCH_README.md, FilterSecu.md, KAFKA_README.md, Nginx개선사항_README.md, REDISAUTH_README.md, REDIS_PRIMARY_REPLICA.md, SHIPMENTSYSTEM_README.md, fream-project-phase1-review.md
- `streaming/`: README.md

## 루트 README (원문, 길이 제한으로 앞부분만)

# Fream Backend

Fream은 확장 가능한 웹 서비스를 위한 견고한 인프라를 제공하는 Spring Boot 기반의 백엔드 애플리케이션입니다. 이 프로젝트는 [이전 버전](https://github.com/junhyeong9812/Improve_Fream_Back)에서 더 나아가 배포 자동화와 운영 효율성을 크게 개선한 버전입니다.



**윈도우 배포 완료** | **[📝 1차 프로젝트 회고록](/docs/portfolio/fream-project-phase1-review.md)**


## 프로젝트 재구성 배경

### 이번 개선의 초점
1. **자동화된 배포 파이프라인**
   - GitHub Actions를 활용한 CI/CD 구축
   - GitHub Container Registry를 통한 체계적인 이미지 관리
   - 무중단 배포 구조 도입

2. **효율적인 개발 프로세스**
   - 기능별 브랜치 전략을 통한 병렬 개발
   - 환경별 설정 분리로 안정적인 운영
   - 모니터링과 로깅 체계 개선

3. **최적화된 인프라 구조**
   - Docker 컨테이너 기반 마이크로서비스 아키텍처
   - AWS 클라우드 인프라 활용
   - 서비스 확장성 고려한 구조 설계


## 시스템 아키텍처

### 시스템 구성도
![image](https://github.com/user-attachments/assets/3b64669d-0452-456c-8f34-8382c17c63fe)



### 주요 컴포넌트 설명

#### 1. 프론트엔드
- **React 애플리케이션**: 사용자 인터페이스 제공
- **Nginx**: 정적 파일 서빙, 리버스 프록시, 캐싱 레이어

#### 2. 백엔드 (Spring Boot)
- REST API 제공
- 비즈니스 로직 처리
- JWT 기반 인증/인가 (OAuth2 소셜 로그인 지원)
- 외부 서비스 연동 (포트원, 이메일 등)
- WebSocket을 통한 실시간 통신

#### 3. 데이터 저장소
- **MySQL**: 영구 데이터 저장
   - 사용자/상품/주문 정보
   - 트랜잭션 처리
- **Redis**: 캐시 및 세션 관리
   - 접근 빈도가 높은 데이터 캐싱
   - 실시간 데이터 처리
- **Elasticsearch**: 검색 엔진
   - Nori 형태소 분석기를 활용한 한글 검색
   - 상품 검색 기능
   - 로그 데이터 분석

#### 4. 메시지 브로커
- **Kafka & Zookeeper**
   - 이벤트 기반 비동기 처리
   - 시스템 간 느슨한 결합
   - 확장성 있는 데이터 파이프라인

#### 5. DevOps
- **GitHub Actions**: CI/CD 파이프라인
   - 자동 빌드/테스트
   - 도커 이미지 생성 및 배포
- **GitHub Container Registry**: 도커 이미지 저장소
- **Certbot**: SSL 인증서 자동 갱신

#### 6. 모니터링
- **Kibana**: 로그 및 검색 데이터 시각화
- **Metricbeat**: 시스템 및 애플리케이션 메트릭 수집

#### 7. 인프라 (AWS)
- **EC2**: 애플리케이션 서버
- **Route 53**: DNS 관리
- **Docker**: 컨테이너 기반 배포

### 주요 기능 흐름

1. **사용자 인증**
   - JWT 기반 인증
   - OAuth2 소셜 로그인 (Google, Naver, Kakao)
   - Redis를 통한 토큰 관리

2. **상품 관리**
   - MySQL: 상품 기본 정보 저장
   - Elasticsearch: 상품 검색 기능
   - Redis: 인기 상품 캐싱

3. **주문/결제 처리**
   - 포트원 결제 시스템 연동
   - Kafka를 통한 비동기 처리
   - 트랜잭션 관리

4. **실시간 처리**
   - Kafka를 통한 이벤트 처리
   - WebSocket을 통한 실시간 알림

### 배포 환경
- **개발 환경**: 로컬 Docker 컨테이너
- **운영 환경**: AWS EC2 + Docker
- **도메인**: www.pinjun.xyz

## 🛠 기술 스택

### 백엔드
- **프레임워크:** Spring Boot 3.4.1
- **개발 언어:** Java 17
- **빌드 도구:** Gradle 8.x
- **ORM:** Spring Data JPA + QueryDSL
- **보안:** Spring Security, JWT

### 데이터베이스 및 캐싱
- **주 데이터베이스:** MySQL 8.0
- **캐시:** Redis 7.0
- **검색 엔진:** Elasticsearch 8.13.4 (Nori 형태소 분석기 포함)

### 메시징
- **이벤트 브로커:** Apache Kafka 7.4.0
- **클러스터 관리:** Zookeeper 7.4.0

### 웹서버 및 프록시
- **웹서버:** Nginx 1.25.2 (캐시 퍼지 모듈 포함)
- **SSL:** Let's Encrypt (Certbot)

### 모니터링 및 로깅
- **시각화:** Kibana 8.10.2
- **메트릭 수집:** Metricbeat 8.13.4
- **로그 수집:** Spring Boot Actuator

### 기타 도구
- **컨테이너화:** Docker
- **CI/CD:** GitHub Actions
- **이미지 저장소:** GitHub Container Registry
- **이메일:** Spring Mail + Gmail SMTP

## 프로젝트 구조

프로젝트는 다음과 같은 구조로 구성되어 있습니다:

```
fream-back/
├── docker/                # 도커 설정 파일들
│   ├── dev/              # 개발 환경 도커 설정
│   │   └── nginx/
│   ├── prod/             # 운영 환경 도커 설정
│   │   └── nginx/
│   │       ├── nginx.conf          # Nginx 설정
│   │       └── certbot/            # SSL 인증서 관리
│   ├── elasticsearch/    # Elasticsearch 도커 설정
│   │   ├── userdict_ko.txt         # 한글 사용자 사전
│   │   └── synonyms.txt            # 동의어 사전
│   ├── kafka/           # Kafka & Zookeeper 도커 설정
│   ├── mysql/           # MySQL 도커 설정
│   └── redis/           # Redis 도커 설정
├── src/
│   ├── main/
│   │   ├── java/com/fream/
│   │   │   ├── config/              # 애플리케이션 설정
│   │   │   ├── controller/          # API 엔드포인트
│   │   │   ├── domain/              # 도메인 모델
│   │   │   ├── repository/          # 데이터 접근 레이어
│   │   │   ├── service/             # 비즈니스 로직
│   │   │   ├── security/            # 인증 및 보안
│   │   │   ├── exception/           # 예외 처리
│   │   │   ├── elasticsearch/       # 검색 엔진 관련
│   │   │   ├── kafka/               # 메시지 큐 관련
│   │   │   ├── util/                # 유틸리티 클래스
