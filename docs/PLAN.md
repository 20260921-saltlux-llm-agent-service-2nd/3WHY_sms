# 지각 잡는 자바 (java-late-catcher) — 프로젝트 계획 초안

> slx-ai-campus에서 Java → MySQL → JDBC를 배운 뒤 만드는 첫 미니 프로젝트.
> `chap99` **피트니스 회원관리 스켈레톤**(메모리 저장)을 가져와 **출석부**로 바꾸고,
> 저장소를 `List<Member>` 에서 **MySQL + JDBC** 로 교체한다.
> **3명이 팀으로** 만들며, GitHub 협업(이슈 → 브랜치 → 커밋 → PR → 리뷰 → 머지)을 연습한다.
> 팀 규칙: [CONVENTION.md](CONVENTION.md)

---

## 1. 한 줄 정의

**스터디 모임의 출석을 콘솔에서 기록하고, 기준 시각보다 늦게 체크인한 사람을 "지각"으로 잡아내는 프로그램.**

---

## 2. 사용 장면 (가정)

- 매일 아침 스터디 시작 전, 총무 한 명이 노트북에서 프로그램을 실행한다.
- 도착한 사람을 고르면 **현재 시각**이 기록되고, 기준 시각(예: 09:00)을 넘었으면 자동으로 `지각`이 찍힌다.
- 모임이 끝나면 "오늘 누가 지각했나", "이번 달 지각왕은 누구인가"를 조회한다.

> 사용자(총무) 1명, 콘솔 메뉴 방식. 로그인·권한·웹 UI는 없음.

---

## 3. 배운 것 → 바꾸는 것

이 프로젝트는 slx-ai-campus의 세 조각을 합친다.

| 출처 | 가져오는 것 |
| --- | --- |
| `01_Java/chap99-Java-Application/src/skeleton/` | `model / repository / view / controller / run` 5층 구조, `Member`, `MembershipType` enum, 콘솔 메뉴 루프 |
| `03_jdbc/06-JDBC-Exercise/` | Gradle 설정, `common.JDBCTemplate`, `config/db.properties`, `mapper/*-query.xml`에 SQL 보관, `PreparedStatement` + `ResultSet → DTO` 매핑 |
| `02_Database/Practice/mmsdb.sql` | `member_tbl` 스키마, FK `ON DELETE CASCADE` 패턴 |

### 클래스 단위 변환표

| chap99 스켈레톤 (피트니스) | 지각 잡는 자바 (출석부) | 무엇이 달라지나 |
| --- | --- | --- |
| `model.Member` (memberId, name, age, phone, membershipType, enrollDate, expiryDate, active) | `model.Member` (memberId, name, phone, joinDate, active) | 회원권 관련 필드 제거. Lombok `@Getter @Setter @AllArgsConstructor @NoArgsConstructor @ToString` 사용 (06-Exercise 방식) |
| `model.MembershipType` enum | `model.AttendanceStatus` enum (`PRESENT("출석")`, `LATE("지각")`, `ABSENT("결석")`) | enum + 한글 설명 패턴 그대로 |
| — | **`model.Attendance`** (attendanceId, memberId, attendDate, checkInTime, status, memo) | 새 DTO |
| `repository.GymDatabase` — `List<Member>` + `nextMemberId` | **`repository.MemberRepository`** — 같은 메서드 이름, 내부만 JDBC | **핵심 변환.** `members.add(m)` → `INSERT`, `for (Member m : members)` → `SELECT … while (rset.next())` |
| — | **`repository.AttendanceRepository`** | 체크인 INSERT, 오늘 현황 JOIN, 지각 횟수 GROUP BY |
| `controller.MemberController` | `controller.MemberController` + **`controller.AttendanceController`** | 지각 판정 로직은 Controller에 둔다 (Service 계층은 아직 안 배웠으므로 도입하지 않음) |
| `view.MemberView` | `view.MemberView` + **`view.AttendanceView`** | 출력 전담 |
| `run.Application` (while + switch 메뉴) | `run.Application` | 메뉴 항목 추가 |
| — | `common.JDBCTemplate`, `config/db.properties`, `mapper/member-query.xml`, `mapper/attendance-query.xml` | 06-Exercise에서 그대로 복사 |

핵심 아이디어 두 가지:

1. **저장소 교체** — 메모리 `List`를 MySQL로 바꾸되, Controller/View는 거의 손대지 않는다. (계층을 나눈 이유를 몸으로 느끼기)
2. **테이블 하나 추가** — 회원 위에 "출석" 테이블을 얹고, 지각 판정 규칙을 넣는다.

---

## 4. 기능 목록

### 필수 (MVP)

1. **회원 관리** — 등록 / 전체 조회 / 이름 검색 / 수정 / 삭제 (스켈레톤 기능 그대로, 저장만 DB)
2. **출석 체크** — 회원 선택 → 현재 시각으로 체크인 → `출석` 또는 `지각` 자동 판정
3. **오늘 출석 현황** — 오늘 날짜 기준 전원 목록 (출석 / 지각 / 미체크) — `LEFT JOIN`
4. **회원별 지각 기록** — 특정 회원의 누적 지각 횟수와 날짜 목록
5. **이번 달 지각 랭킹** — 지각 횟수 내림차순 (지각왕) — `GROUP BY … ORDER BY COUNT(*) DESC`

### 선택 (여유 있으면)

- 기준 시각(09:00)을 `db.properties` 옆 설정 파일로 빼기
- 결석 마감 — 그날 체크인 없는 사람을 `ABSENT`로 일괄 INSERT
- 지각 사유(memo) 입력
- 월별 출석률(%) 통계
- 활성 회원만 보는 `VIEW` 만들어 보기 (04_DDL/03_View 복습)

### 하지 않는 것

- 웹/GUI, 로그인, 여러 모임 관리, 실시간 알림
- Service 계층, 트랜잭션, 커넥션 풀 — 아직 안 배운 것은 넣지 않는다

---

## 5. 데이터 설계

`mmsdb.sql` 스타일(`_tbl` 접미사, `IF NOT EXISTS`, FK CASCADE)을 따른다.

```sql
CREATE DATABASE IF NOT EXISTS latedb DEFAULT CHARACTER SET utf8mb4;
USE latedb;

CREATE TABLE IF NOT EXISTS member_tbl (
    member_id  INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50)  NOT NULL,
    phone      VARCHAR(20)  NOT NULL UNIQUE
               CHECK (phone REGEXP '^[0-9]{3}-[0-9]{4}-[0-9]{4}$'),
    join_date  DATE DEFAULT (CURRENT_DATE) NOT NULL,
    active     BOOLEAN DEFAULT TRUE NOT NULL
);

CREATE TABLE IF NOT EXISTS attendance_tbl (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    member_id     INT  NOT NULL,
    attend_date   DATE NOT NULL,
    check_in_time TIME,
    status        ENUM('PRESENT', 'LATE', 'ABSENT') NOT NULL,
    memo          VARCHAR(200),
    CONSTRAINT fk_attendance_member FOREIGN KEY (member_id)
        REFERENCES member_tbl (member_id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT uk_member_date UNIQUE (member_id, attend_date)   -- 하루 한 번만 체크인
);
```

**지각 판정 규칙** (`AttendanceController`에 둔다, SQL에 두지 않는다):

```text
check_in_time <= 09:00:00  →  PRESENT
check_in_time >  09:00:00  →  LATE
그날 기록 없음              →  (조회 시) 미체크 / 마감하면 ABSENT
```

**날짜·시간 ↔ JDBC** — 이 프로젝트에서 처음 다루는 부분:

| Java | MySQL | 넣을 때 | 꺼낼 때 |
| --- | --- | --- | --- |
| `LocalDate` | `DATE` | `pstmt.setObject(i, localDate)` | `rset.getObject("attend_date", LocalDate.class)` |
| `LocalTime` | `TIME` | `pstmt.setObject(i, localTime)` | `rset.getObject("check_in_time", LocalTime.class)` |
| enum | `ENUM` / `VARCHAR` | `pstmt.setString(i, status.name())` | `AttendanceStatus.valueOf(rset.getString("status"))` |

---

## 6. 프로젝트 구조 (06-JDBC-Exercise 레이아웃 그대로)

```text
java-late-catcher/
├── build.gradle                 # 06-Exercise 복사: mysql-connector-j, lombok, junit
├── settings.gradle
├── sql/
│   └── schema.sql               # 위 CREATE 문 + 샘플 회원 INSERT
├── docs/
│   └── PLAN.md                  # 이 문서
└── src/main/java/
    ├── config/
    │   ├── db.properties.example    # 커밋함 (url, user만)
    │   └── db.properties            # 커밋 안 함 — 실제 비밀번호
    ├── mapper/
    │   ├── member-query.xml
    │   └── attendance-query.xml
    ├── common/
    │   └── JDBCTemplate.java
    ├── model/
    │   ├── Member.java
    │   ├── Attendance.java
    │   └── AttendanceStatus.java
    ├── repository/
    │   ├── MemberRepository.java
    │   └── AttendanceRepository.java
    ├── controller/
    │   ├── MemberController.java
    │   └── AttendanceController.java
    ├── view/
    │   ├── MemberView.java
    │   └── AttendanceView.java
    └── run/
        └── Application.java
```

> 06-Exercise처럼 `db.properties`와 `*.xml`을 `src/main/java` 아래에 두고 상대 경로로 읽는다.
> 그래서 **실행 작업 디렉터리는 항상 프로젝트 루트**여야 한다 (IntelliJ 기본값).
> `.gitignore`에 `src/main/java/config/db.properties`를 추가한다.

---

## 7. 콘솔 메뉴 (초안)

chap99 `Application`의 `while (true) { … switch (choice) }` 패턴 그대로.

```text
===== 지각 잡는 자바 =====
1. 회원 관리
2. 출석 체크
3. 오늘 출석 현황
4. 회원별 지각 기록
5. 이번 달 지각 랭킹
9. 프로그램 종료
선택 >
```

`1. 회원 관리`는 chap99처럼 하위 메뉴(등록/조회/수정/삭제)로 들어간다.

출석 체크 화면 예:

```text
[출석 체크] 2026-10-20 09:07:31
1. 김철수   2. 이영희   3. 박민수
번호 선택 > 2
→ 이영희 : 09:07:31 체크인 — ⚠ 지각 (기준 09:00)
```

---

## 8. 팀 구성과 역할 분담 (3명)

규칙은 [CONVENTION.md](CONVENTION.md)에 있다. 여기서는 **누가 무엇을** 맡는지만 정한다.

### 나누는 원칙 — 층(layer)이 아니라 기능(feature)으로 나눈다

"A는 model, B는 repository, C는 controller" 식으로 층을 나누면 서로 기다리느라 아무도 못 움직인다.
대신 **한 사람이 한 기능을 model → repository → controller → view 까지 세로로** 만든다.
그래야 PR 하나가 "동작하는 기능 하나"가 되고, 리뷰어도 흐름을 따라 읽을 수 있다.

### 역할 (이름은 A/B/C로 두고 팀에서 정한다)

| 담당 | 기능 | 만드는 파일 | 핵심 SQL |
| --- | --- | --- | --- |
| **A — 회원** | 회원 등록/조회/검색/수정/삭제 (chap99 이식) | `MemberRepository`, `MemberController`, `MemberView`, `member-query.xml` | `INSERT`, `SELECT`, `UPDATE`, `DELETE`, `LIKE` |
| **B — 출석** | 출석 체크, 지각 판정, 중복 체크인 방지 | `AttendanceRepository`(INSERT·단건 조회), `AttendanceController`(판정 로직), `AttendanceView`, `attendance-query.xml` | `INSERT`, `UNIQUE` 위반 처리, `TIME` ↔ `LocalTime` |
| **C — 통계 + 통합** | 오늘 현황, 회원별 지각 기록, 이번 달 랭킹, `run.Application` 메뉴 통합, README | `AttendanceRepository`(조회 메서드 추가), `StatsView`, `run/Application` | `LEFT JOIN`, `GROUP BY`, `ORDER BY COUNT(*)`, `MONTH()` |

> B와 C가 `AttendanceRepository` 한 파일을 같이 건드린다. 메서드가 겹치지 않으면 git이 알아서 합치고,
> 겹치면 **첫 충돌 해결 연습**이 된다. 피하려 하지 말고 겪어 본다.

### 함께 먼저 하는 것 (0단계 — 다른 모든 작업의 전제)

세로로 나눠도 공통 뼈대는 있어야 한다. 0단계는 **세 개의 작은 이슈로 나눠서 각자 한 번씩 전체 흐름(이슈 → 브랜치 → PR → 리뷰 2명 → 머지)을 연습**한다.

| 이슈 | 담당 | 내용 | 라벨 |
| --- | --- | --- | --- |
| #1 | A | Gradle 프로젝트 생성, `build.gradle`, `common/JDBCTemplate`, `config/db.properties.example`, `.gitignore`에 `db.properties` 추가 | `🌏 Environment` `config` |
| #2 | B | `sql/schema.sql` (DB·사용자 생성 + 테이블 + 샘플 회원 5명), `model/Member`, `model/Attendance`, `model/AttendanceStatus` | `🌏 Environment` `model` |
| #3 | C | GitHub 설정 — 라벨, 이슈·PR 템플릿(`.github/`), main 브랜치 보호(approve 2), README 초안, `run/Application` 메뉴 뼈대(각 번호는 "준비 중" 출력) | `🌏 Environment` `run` |

model 클래스를 0단계에서 B가 만드는 이유: A·B·C 모두 `Member`/`Attendance`를 쓰기 때문에 **필드 이름을 먼저 합의**해야 한다. 5절의 스키마가 기준이다.

### 흐름

```text
0단계 (3명 병렬, 각 1 PR) ─┬─ A: 회원 관리 ─────────┐
                           ├─ B: 출석 체크 + 판정 ──┼─ C: 메뉴 통합 ─ [v0.1] ─ 다듬기(선택 기능을 이슈로 나눠 각자) ─ [v0.2]
                           └─ C: 통계 조회 ─────────┘
```

- 0단계 PR 3개가 모두 머지된 뒤 기능 작업을 시작한다.
- A·B·C는 각각 **기능 1개 = 이슈 1개 = 브랜치 1개 = PR 1개**를 원칙으로 하되, 너무 크면 이슈를 쪼갠다 (예: A는 "회원 등록·조회" / "회원 수정·삭제" 2개로).
- C의 메뉴 통합 PR은 A·B 머지 후 마지막에 한다.
- 선택 기능(4절)은 `✨ Feature` 이슈로 올려 두고, 먼저 끝난 사람이 가져간다.
- 매일 한 번, 짧게(10분) 모여서 열린 PR을 함께 보고 approve한다. 3명 팀에서 approve 2개 = **나머지 두 명이 모두 봐야 머지된다.** 느리지만 그게 연습이다.

---

## 9. 기술 선택 (slx-ai-campus와 동일하게)

- **Java 21** (temurin-21), **MySQL 8**, **Gradle** (wrapper), `com.mysql:mysql-connector-j:9.7.0`
- **Lombok** `1.18.46` — DTO 보일러플레이트 제거 (06-Exercise에서 쓴 방식)
- **JUnit 6** — `build.gradle`에 이미 들어 있음. 처음엔 콘솔로 확인하고, `AttendanceController`의 지각 판정만 단위 테스트 1~2개 써 보면 좋은 연습
- SQL은 Java 코드가 아니라 `mapper/*.xml`에 둔다 (06-Exercise 방식)
- 자원 해제: `finally { close(rset); close(pstmt); close(con); }` (06-Exercise) 또는 try-with-resources (05-JDBC) — **하나로 통일**해서 쓴다. 추천: try-with-resources.
- DB 접속 정보는 `db.properties`에 두고 git에서 제외. `db.properties.example`만 커밋.

---

## 10. 성공 기준

- [ ] chap99 회원 CRUD가 메모리 대신 MySQL에 저장되고, 프로그램을 재시작해도 데이터가 남는다
- [ ] 출석 체크 시 기준 시각에 따라 `PRESENT` / `LATE`가 자동으로 저장된다
- [ ] 같은 사람이 같은 날 두 번 체크인하면 막히고, 친절한 메시지가 나온다
- [ ] "이번 달 지각 랭킹"이 정렬되어 나온다
- [ ] 팀원 각자 PR 2개 이상, 모든 PR이 approve 2개를 받고 머지됐다, 태그 1개 이상이 남는다
- [ ] 모든 커밋이 `태그: 제목` + 본문 형식을 지킨다 ([CONVENTION.md](CONVENTION.md))
- [ ] README만 보고 다른 사람이 clone → `schema.sql` 실행 → `db.properties` 작성 → 실행 할 수 있다

---

## 11. 열린 질문 (정해지면 이 문서를 고친다)

1. 기준 시각은 09:00 하나로 충분한가, 요일별로 다른가?
2. `Member`에서 `age`를 뺐는데, 스켈레톤과 똑같이 두는 게 이식 연습에 더 편한가?
3. 회원 삭제 시 출석 기록도 함께 지우는 `ON DELETE CASCADE`로 갈 것인가, 기록을 남길 것인가?
4. 자원 해제 스타일 — try-with-resources로 통일해도 되는가?
5. JUnit 테스트를 이번 범위에 넣을 것인가?
6. A/B/C는 누가 맡는가? (첫 모임에서 정한다)
7. 머지 방식 — merge commit으로 갈 것인가, squash로 갈 것인가? (CONVENTION.md에는 merge commit을 제안해 두었다)
