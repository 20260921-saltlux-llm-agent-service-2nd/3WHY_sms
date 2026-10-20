# GIT 컨벤션 — java-late-catcher 팀 규칙

> 팀 3명. 각자 기능을 나눠 개발하고, PR을 올리고, **리뷰는 함께** 한다.
> 이 문서는 팀의 공통 GitHub 컨벤션을 이 프로젝트에 맞게 정리한 것이다.
> 역할 분담은 [PLAN.md 8절](PLAN.md#8-팀-구성과-역할-분담-3명)에 있다.

---

## 한눈에 보기

```text
이슈 작성 ─→ 브랜치 생성 ─→ 커밋 ─→ PR 작성 ─→ 리뷰 (approve 2) ─→ main 머지 ─→ 이슈 자동 close
 #12          feature/#12-member-crud     feat: …     제목 끝에 #12        매일 1회 확인      close #12
```

---

## 1. Commit Rule

- 설명은 **한글**로 쓴다.
- 하나의 커밋 = 하나의 기능(또는 하나의 파일). 여러 기능을 한 커밋에 섞지 않는다.
- **header만 적지 말고 body까지 꼭 적는다.**

### Header — `태그: 제목`

`:` 뒤에만 공백이 있다. (`feat:로그인` ✗, `feat : 로그인` ✗, `feat: 로그인` ✓)

| 태그 | 언제 |
| --- | --- |
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 (삭제 포함) |
| `docs` | 문서 수정 (README, PLAN, 주석) |
| `refactor` | 동작은 같고 구조·이름만 바꿀 때 |
| `test` | 테스트 추가·수정 |
| `chore` | 빌드 설정, `.gitignore`, 템플릿 등 코드 외 작업 |

### Body

- 양에 구애받지 말고 **최대한 상세히** 쓴다.
- **어떻게**보다 **무엇을, 왜** 바꿨는지 쓴다.
- 한 줄 띄우고 `-`로 시작하는 목록으로 쓴다.

```text
feat: 회원 등록 기능을 MySQL에 저장하도록 구현

- chap99 스켈레톤의 GymDatabase.addMember()는 List에만 저장해서
  프로그램을 끄면 데이터가 사라졌다.
- MemberRepository.addMember()에서 member-query.xml의 insertMember
  쿼리를 PreparedStatement로 실행하도록 바꿨다.
- phone이 UNIQUE 제약에 걸리면 SQLException이 나는데, 지금은
  스택트레이스만 출력한다. 사용자 메시지는 #15에서 다룬다.
```

```text
fix: 체크인 시각이 09:00 정각일 때 지각으로 판정되던 문제 수정

- LocalTime.isAfter()는 같은 시각이면 false인데, 비교 방향을
  반대로 써서 09:00:00 체크인이 LATE로 저장됐다.
- 기준 시각 "이하"는 PRESENT가 되도록 조건을 바꿨다.
```

---

## 2. Branch Rule

### 이름 — `종류/#이슈번호-영어-설명`

```text
feature/#12-member-crud
feature/#13-attendance-checkin
fix/#21-late-boundary
refactor/#30-try-with-resources
test/#33-late-judge-test
```

| 종류 | 언제 |
| --- | --- |
| `feature` | 새 기능 |
| `fix` | 버그 수정 |
| `refactor` | 구조 변경 |
| `test` | 테스트 |

- 브랜치는 **반드시 관련 이슈 번호**를 넣는다. 이슈 없는 브랜치는 만들지 않는다.
- 영어 단어 2~3개, 소문자, 하이픈(`-`)으로 연결한다.

### 흐름 — `기능 브랜치` → `main`

- `main`에는 **정말 필요하고 안전한 코드만** 있어야 한다.
- `main`에 직접 push하지 않는다. (브랜치 보호 설정으로 막아 둔다)
- 브랜치는 항상 **최신 `main`에서** 딴다. 작업이 길어지면 `main`을 자주 당겨온다(`git pull origin main` 또는 rebase).
- 머지된 브랜치는 지운다.

```bash
git switch main
git pull origin main
git switch -c feature/#12-member-crud
# ... 작업, 커밋 ...
git push -u origin feature/#12-member-crud
```

---

## 3. Issue Rule

### 라벨 — 두 종류를 함께 붙인다

#### 개발 파트 (이 프로젝트의 패키지 기준)

| 라벨 | 범위 |
| --- | --- |
| `common` | `JDBCTemplate` 등 공통 유틸 |
| `config` | `db.properties`, `build.gradle`, `.gitignore` |
| `sql` | `sql/schema.sql`, `mapper/*.xml` |
| `model` | DTO, enum |
| `repository` | JDBC 접근 |
| `controller` | 판정·흐름 로직 |
| `view` | 콘솔 출력 |
| `run` | `Application` 메뉴 |
| `docs` | README, PLAN, CONVENTION |

#### 이슈 종류

| 라벨 | 언제 |
| --- | --- |
| `✨ Feature` | 새로운 기능 또는 명세가 추가될 때 |
| `🐞 Bug` | 버그 |
| `🔨 Refactor` | 리팩터링 |
| `🌏 Environment` | 환경 설정, 초기 세팅 |
| `🧪 Test` | 테스트 추가 |
| `💬 Suggestion` | 팀원들과 논의할 주제, 고찰점, 제안 |

### 제목

이슈의 내용이 **한 줄로 명확하게** 드러나게 쓴다. 번호는 GitHub가 붙여 준다.

```text
회원 등록/조회를 MySQL에 저장하도록 구현
체크인 09:00 정각이 지각으로 판정됨
지각 기준 시각을 요일별로 다르게 할지 논의
```

### 내용 — 템플릿을 쓴다

`.github/ISSUE_TEMPLATE/` 에 종류별 템플릿을 둔다 (0단계 이슈 #3에서 만든다). 각 템플릿이 받는 것:

| 템플릿 | 항목 |
| --- | --- |
| ✨ Feature | 📄 Describe (기능 설명) / ✅ Tasks (세분화한 할 일 체크리스트) / 🙋🏻 More |
| 🐞 Bug | 🐞 버그 설명 / ✅ 예상 결과 / ❗ 실제 결과 / 💻 재현 단계 / 📄 로그 / 🌏 환경 / 🙋🏻 More |
| 🔨 Refactor | 🔨 Describe (왜 리팩터링하나) / ✅ Tasks / 🙋🏻 More |
| 🌏 Environment | 🤔 Describe (현재 상황) / ✅ Tasks / 🙋🏻 More |
| 💬 Suggestion | 💬 주제 / 🤔 선택지 / 🙋🏻 More |

#### Feature 이슈 예

```markdown
labels: ✨ Feature, repository, controller, view
제목: 회원 등록/조회를 MySQL에 저장하도록 구현

## 📄 Describe
chap99 회원관리 스켈레톤의 GymDatabase(List 저장)를 MemberRepository(JDBC)로
교체한다. Controller/View는 메서드 시그니처를 유지해 거의 손대지 않는다.

## ✅ Tasks
- [ ] mapper/member-query.xml 에 insertMember, selectAllMember 작성
- [ ] MemberRepository.addMember() — INSERT
- [ ] MemberRepository.getAllMembers() — SELECT, ResultSet → Member 매핑
- [ ] Application 메뉴 1-1, 1-2 에서 동작 확인

## 🙋🏻 More
수정/삭제는 별도 이슈(#14)로 뺐다. 이 이슈는 등록·조회만.
```

---

## 4. PR Rule

1. **이슈를 먼저** 만든다. 무엇을 구현할지 이슈로 적은 뒤 브랜치를 딴다.
2. PR **제목 끝에 공백 + `#이슈번호`** 를 붙인다. → `feat: 회원 등록/조회 MySQL 저장 구현 #12`
3. PR 본문에 **`close #이슈번호`** 를 쓰면 머지 시 이슈가 자동으로 닫힌다.
4. 라벨은 이슈와 같은 것을 붙인다 (`✨ Feature` / `🐞 Bug` / `🔨 Refactor` / `🌏 Environment`).
5. PR 하나는 **동작하는 기능 하나**. 리뷰어가 20분 안에 읽을 수 있는 크기로 유지한다. 커지면 이슈를 쪼갠다.
6. 머지 방식은 **Merge commit** (squash 아님) — 공들여 쓴 커밋 body를 `main` 히스토리에 남기기 위해서다. *(제안. 팀에서 확정)*

### PR 템플릿 (`.github/PULL_REQUEST_TEMPLATE.md`)

```markdown
## 📄 Summary
- 어떤 부분을 수정/추가했는지 작성해 주세요

## ✅ 확인 방법
- 리뷰어가 로컬에서 어떻게 돌려 보면 되는지 (메뉴 번호, 입력값, 기대 출력)

## 🙋🏻 To Reviewers
- 리뷰어가 주의 깊게 볼 필요가 있는 부분을 작성해 주세요

close #
```

> `✅ 확인 방법`은 원본 컨벤션에 없던 항목을 추가한 것이다. 콘솔 앱은 스크린샷이 없으니 리뷰어가 직접 돌려 봐야 하고, 그 방법을 PR에 적어 두면 리뷰가 빨라진다.

---

## 5. Code Review

- **매일 하루에 한 번** 열린 PR을 확인한다. 짧게 모여서(10분) 같이 보는 걸 권장.
- **approve 2개 이상**이어야 머지할 수 있다. (main 브랜치 보호 규칙으로 강제)
  - 3명 팀이므로 **작성자를 제외한 두 명이 모두 approve** 해야 한다.
  - 본인 PR은 본인이 approve할 수 없다.
- 머지는 **approve가 다 모인 뒤 작성자가** 누른다.
- 리뷰 코멘트는 "왜"를 함께 쓴다. 단순 취향 차이는 `nit:` 접두어로 표시해 필수 아님을 밝힌다.
- 리뷰어가 볼 것:
  - 이슈의 Tasks가 모두 체크됐나
  - 커밋 메시지가 규칙(`태그: 제목` + body)을 지켰나
  - `db.properties`, 비밀번호가 커밋에 섞이지 않았나
  - PR의 `✅ 확인 방법`대로 돌려 봤을 때 실제로 동작하나

---

## 6. GitHub 저장소 설정 체크리스트 (0단계 이슈 #3)

- [ ] 라벨 생성 — 위 3절의 파트 9개 + 종류 6개
- [ ] `.github/ISSUE_TEMPLATE/` — feature / bug / refactor / environment / suggestion 5개 (`.yml`)
- [ ] `.github/PULL_REQUEST_TEMPLATE.md`
- [ ] `main` 브랜치 보호: PR 필수, approve 2 필수, 직접 push 금지, 머지 전 최신 main 반영 필수
- [ ] Settings → Pull Requests: Merge commit만 허용, 머지 후 브랜치 자동 삭제
- [ ] (선택) Projects 보드 — Todo / In Progress / In Review / Done
