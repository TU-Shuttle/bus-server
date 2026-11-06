# bus-server

TU Shuttle(한국공대 셔틀버스) 백엔드. Java 25, Spring Boot 4.1, MySQL.

## 구조

```
기사 앱 ──WebSocket(/ws, X-Driver-Token)──▶ 서버(메모리에 최신 위치) ──SSE / REST──▶ 학생 앱
학교 홈페이지 PDF ──(배치 / 수동 트리거)──▶ 서버(파싱) ──▶ MySQL ──REST──▶ 학생 앱
```

| 모듈 | 역할 |
| --- | --- |
| `location` | 기사 앱 위치 수신(WebSocket), 최신 위치 보관, 학생 앱에 푸시(SSE)·조회(REST) |
| `schedule` | 학교 PDF 시간표 수집·파싱·저장, 시간표 조회 |
| `naver` | 네이버 Directions API 중계 |
| `user` | 회원가입/로그인 |

### 실시간 위치

- **업링크**: 기사 앱이 `ws://<host>/ws` 에 `X-Driver-Token` 헤더와 함께 연결해 `{"id":"bus1","lat":37.34,"lng":126.73,"accuracy":5,"status":"RUNNING"}` 를 보낸다(`accuracy`, `status`는 생략 가능, `status`는 `RUNNING`/`WAITING`/`STOP`).
  토큰이 없거나 틀리면 핸드셰이크가 401 로 거절된다. 잘못된 메시지는 `{"error":"..."}` 로 응답하고 연결은 유지한다.
- **다운링크**: 학생 앱은 SSE 로 받는다. 끊기면 재연결하면 되고, 연결 직후 현재 위치를 한 번 받는다.
- 위치는 메모리에만 보관한다. 마지막 수신 후 `shuttle.realtime.stale-after`(기본 60초)가 지나면 운행 정보 없음으로 본다.
- 이 구조는 단일 서버 기준이다. 서버를 여러 대로 늘리면 위치 저장소(Redis 등)와 SSE 브로드캐스트를 공유해야 한다.

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| GET | `/api/bus/{id}/location` | 최신 위치. 없거나 오래됐으면 404 |
| GET | `/api/bus/locations` | 운행 중인 모든 버스 위치 |
| GET | `/api/bus/{id}/location/stream` | 한 버스 SSE 구독(`location` 이벤트) |
| GET | `/api/bus/location/stream` | 전체 버스 SSE 구독 |

### 시간표

학교 [셔틀버스 페이지](https://tukorea.ac.kr/tukorea/1136/subview.do)의 첨부 PDF를 내려받아 좌표 기반으로 해석한다(`PdfShuttleTimetableParser`).

- 매일 04:00(KST)에 수집하고, 활성 시간표가 하나도 없으면 기동 직후에도 한 번 수집한다.
- 원본 파일의 SHA-256 이 같으면 다시 반영하지 않는다.
- 다운로드나 파싱이 실패하면(학교가 PDF 레이아웃을 바꾼 경우 등) DB 를 건드리지 않고 **기존 시간표를 유지**한다. 이 경우 파서를 고쳐야 한다.
- 수동 수집: `POST /api/admin/schedule/refresh` (`X-Admin-Token` 헤더 필요)
- 파서가 같은 PDF를 다르게 읽도록 바뀌면 `ShuttleTimetableIngestServiceImpl.PARSER_VERSION`을 올린다. 해시에 섞여 있어 다음 수집 때 다시 반영된다.

#### 요일별 추가 운행 (학사 공지)

조기취업학부 제2캠퍼스 셔틀처럼 학교 셔틀 페이지가 아닌 학사 공지로 따로 나오는 시간표는 `shuttle_extra_schedule`에 둔다.
경로(방향)는 고정이고 학기마다 시각만 바뀌므로, 시각을 고친 JSON을 관리자 API로 통째로 올리면 된다.

- 기본값: `src/main/resources/timetable/extra-schedule.json`(26-2학기). 테이블이 비어 있으면 기동 때 넣는다.
- 교체: `PUT /api/admin/schedule/extra` (`X-Admin-Token` 필요, 본문은 기본값 파일과 같은 형식, `days`는 `MONDAY`…`SUNDAY`)
- `/api/bus/timetable` 응답에 합쳐진다. 요일별 운행은 `daysOfWeek`가 채워져 있고, 같은 방향·시각이 정규 시간표에도 있으면 추가 운행 쪽이 남는다.
- 적용 기간(`validTo`)이 지나면 응답에서 빠진다.

```bash
curl -X PUT -H "X-Admin-Token: $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d @src/main/resources/timetable/extra-schedule.json http://localhost:8080/api/admin/schedule/extra
```

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| GET | `/api/bus/timetable` | 현재 시간표 전체(제목·적용 기간·수집 시각·모든 출발 시각). 없으면 404 |
| GET | `/api/bus/schedule?dayType=&direction=` | 활성 시간표 조회. `dayType`: `WEEKDAY`/`SATURDAY`, `direction`: `STATION_TO_CAMPUS1` 등 |
| POST | `/api/admin/schedule/refresh` | 시간표 즉시 수집 (관리자) |
| PUT | `/api/admin/schedule/extra` | 요일별 추가 운행 교체 (관리자) |

`endTime` 이 있는 항목은 정시 출발이 아니라 수시운행 구간(`departTime`~`endTime`)이다.

## 로컬 실행

```bash
cp .env.example .env        # DB_PASSWORD, DB_ROOT_PASSWORD, ADMIN_TOKEN, DRIVER_TOKEN 등을 채운다
docker compose up -d        # MySQL (localhost:58816)
./gradlew bootRun           # profile: local, http://localhost:8080
```

JDK 25 가 없으면 Gradle toolchain 이 자동으로 내려받는다. Swagger UI: `/swagger-ui`.

## 환경 변수

| 이름 | 설명 |
| --- | --- |
| `DB_USERNAME`, `DB_PASSWORD`, `DB_ROOT_PASSWORD`, `DB_PORT` | MySQL (docker-compose 와 application-*.yml 공용) |
| `NAVER_MAP_CLIENT_ID`, `NAVER_MAP_CLIENT_SECRET` | 네이버 Directions API |
| `ADMIN_TOKEN` | 관리자 API 토큰. 비어 있으면 관리자 API 는 모두 거절된다 |
| `DRIVER_TOKEN` | 기사 앱 WebSocket 토큰. 비어 있으면 모든 연결이 거절된다 |
| `DOCKERHUB_USERNAME`, `DOCKERHUB_PASSWORD` | Jib 이미지 배포 시에만 필요 |

## 테스트

```bash
./gradlew test
```

시간표 파서는 실제 학교 PDF(`src/test/resources/timetable/shuttle-2026-2.pdf`)로 검증한다.
학교가 새 학기 시간표를 올려 형식이 달라지면 이 테스트가 먼저 깨지도록 두었다.
