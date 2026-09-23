# Google Calendar 연동 기능 구현 정리

## 1. 개요

기존 Google OAuth 로그인 위에, 로그인한 사용자의 **Google Calendar 일정을 읽기 전용으로 조회**해서
DayPlanner의 Todo와 함께 캘린더 화면에 보여주는 기능을 구현했다.

설계 방침(사전 확정 사항):
- Google Calendar Event는 Todo와 **별개 데이터**로 취급 — Todo DB에 저장하지 않고, 매 요청마다 구글 API를
  실시간으로 호출해서 화면에만 보여준다(강제 동기화 없음).
- scope가 `calendar.readonly`(읽기 전용)이므로, 폼 로그인 사용자와 구글 로그인 사용자를 권한상
  차별화할 필요가 없다고 판단 — 구글 토큰이 있으면 캘린더가 보이고, 없으면(폼 로그인) 그 영역만 비어있는
  방식으로 단순화.

---

## 2. Google Cloud Console 설정

1. **OAuth 동의 화면 > 데이터 액세스(Data Access)** 탭에서 범위 추가
   - `.../auth/calendar.readonly` (Calendar API의 "민감한 범위") 선택 — 공유 권한 조회용인
     `calendar.acls.readonly`는 불필요해서 제외
2. `access_denied` / "Google 인증 절차를 완료하지 않았습니다" 오류가 발생했던 원인은 scope 문제가 아니라
   **OAuth 동의 화면 > 대상(Audience) 탭의 테스트 사용자 목록**에 본인 계정이 빠져 있었기 때문이었음
   (Data Access 탭과 Audience 탭이 분리돼 있어 헷갈리기 쉬움)
3. Calendar API는 라이브러리 연동 전 미리 활성화해둠(OAuth 로그인 작업 때 완료)

## 3. application.properties — scope 확장

```properties
spring.security.oauth2.client.registration.google.scope=email,profile,https://www.googleapis.com/auth/calendar.readonly
```

> scope는 **로그인 시점**에 사용자가 동의한 범위만큼만 access token에 담기므로, scope를 추가한 뒤에는
> 기존 로그인 세션을 로그아웃하고 재로그인해야 새 권한이 담긴 토큰을 받을 수 있다.

---

## 4. pom.xml — 의존성 추가

```xml
<dependency>
    <groupId>com.google.api-client</groupId>
    <artifactId>google-api-client</artifactId>
    <version>2.2.0</version>
</dependency>
<dependency>
    <groupId>com.google.apis</groupId>
    <artifactId>google-api-services-calendar</artifactId>
    <version>v3-rev20220715-2.0.0</version>
</dependency>
<dependency>
    <groupId>com.google.oauth-client</groupId>
    <artifactId>google-oauth-client</artifactId>
    <version>1.34.1</version>
</dependency>
<dependency>
    <groupId>com.google.auth</groupId>
    <artifactId>google-auth-library-oauth2-http</artifactId>
    <version>1.19.0</version>
</dependency>
```

- 앞의 세 개는 Calendar API 호출의 기본 골격(HTTP 클라이언트, Calendar 전용 모델 클래스, OAuth 유틸)
- `google-auth-library-oauth2-http`는 인증 처리에 사용 — 아래 5-2 참고(`GoogleCredential`이 deprecated라
  최신 라이브러리로 교체)

---

## 5. 클래스 구조

### 5-1. GoogleEventDto (신규)
구글 Calendar API의 `Event` 리소스(`getId`, `getSummary`, `getStart` 등 구글이 정한 필드/getter명)를
그대로 받아서, 프로젝트의 스네이크케이스 네이밍에 맞춰 변환해 담는 그릇. DB와 무관한 순수 조회용 DTO.

```java
private String event_id;
private String calendar_id;
private String title;
private String description;
private String start;    // ISO 8601 문자열
private String end;
private boolean all_day; // 구글 응답엔 없는 필드, start.getDate() 존재 여부로 직접 판별
```

### 5-2. CalendarService / CalendarServiceImpl (신규)
기존 프로젝트의 Service/ServiceImpl 분리 패턴을 따름.

- `getEvents(Authentication authentication)`: 폼 로그인 사용자(`OAuth2AuthenticationToken`이 아닌 경우)는
  빈 리스트 반환(예외 아님)
- `OAuth2AuthorizedClientService`로 로그인한 사용자의 구글 access token을 꺼내옴
- **인증 처리**: 처음엔 `GoogleCredential`(deprecated)을 썼다가, `GoogleCredentials` +
  `HttpCredentialsAdapter`(google-auth-library) 조합으로 교체
  ```java
  GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(accessToken, null));
  new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(),
          new HttpCredentialsAdapter(credentials))
          .setApplicationName("DayPlanner")
          .build();
  ```
- **조회 범위**: 처음엔 `setTimeMin(현재 시각)`으로 "지금 이후"만 가져왔다가, 지난달 데이터가 안 보이는
  문제로 `timeMin`을 최근 두 달 전 시점으로 조정, `maxResults`도 50 → 100으로 확대
- 캘린더 조회 실패(토큰 문제 등)는 예외를 잡아 빈 리스트로 흡수 — Todo 화면 자체는 깨지지 않게 함
- `Event` → `GoogleEventDto` 변환(`toDto()`)에서 `getSummary()` → `title`, `getId()` → `event_id` 등으로
  구글 쪽 명명과 프로젝트 명명을 매핑

### 5-3. TodoMapper / TodoService / TodoServiceImpl — 전체 조회 메서드 추가
캘린더 화면은 페이징 없이 사용자의 전체 Todo가 필요해서, 기존 페이징 전용 `selectTodoList`와 별개로
`selectAllTodoByUser`를 새로 추가.

```xml
<select id="selectAllTodoByUser" resultType="kr.or.oti.project.domain.Todo">
    SELECT todo_id, user_no, title, content, schedule_date, status, file_url, file_name
    FROM TODO WHERE user_no = #{user_no} ORDER BY schedule_date ASC
</select>
```

```java
// TodoServiceImpl - 기존 getTodoList와 동일한 변환 방식(TodoResponseDto::from) 재사용
public List<TodoResponseDto> getAllTodoByUser(Long user_no) {
    List<Todo> todos = todoMapper.selectAllTodoByUser(user_no);
    return todos.stream().map(TodoResponseDto::from).collect(Collectors.toList());
}
```

### 5-4. CalendarController (신규)
`/calendar` 단일 GET 매핑. `@AuthenticationPrincipal CustomUserDetails`로 Todo 조회용 user_no를,
`Authentication` 파라미터로 구글 토큰 판별용 인증 정보를 각각 받아 Todo/GoogleEvent를 따로 조회해
모델에 담아 넘김(두 데이터를 DB 레벨에서 합치지 않는 설계 방침 유지).

### 5-5. calendar.html (신규)
Thymeleaf 서버사이드 렌더링 구조라 FullCalendar.js를 CDN 스크립트 태그로 연동(Highcharts와 동일한 방식).
Todo는 파란색, Google Event는 초록색 계열로 구분해서 한 캘린더에 표시.

```html
<script src="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.17/index.global.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.17/locales/ko.global.min.js"></script>
```

---

## 6. 겪은 오류와 해결

### 6-1. `403 access_denied` (Google 인증 절차 미완료)
- Calendar API "사용 설정"(활성화)과 "동의 화면 scope 등록"은 별개 설정이라는 걸 혼동
- 실제 원인은 scope 문제가 아니라 **Audience 탭의 테스트 사용자 목록 누락** — 재등록으로 해결

### 6-2. FullCalendar CDN 링크 깨짐
- 처음 안내한 URL에 버전 번호 자리가 실제 값으로 안 채워진 오타(`[email protected]` 형태)가 있어서
  네트워크 오류 발생
- FullCalendar 6버전은 `main.min.js`/`main.min.css` 분리 방식이 아니라 `index.global.min.js` 단일
  번들(CSS 포함)이라는 점도 함께 정정
- 최종적으로 `fullcalendar@6.1.17/index.global.min.js` + `locales/ko.global.min.js`로 정상 동작 확인

### 6-3. 지난달 일정이 화면에 안 보임
- 원인: `setTimeMin(현재 시각)`으로 "지금 이후" 일정만 조회하도록 만들어둔 상태였음(의도된 동작)
- 해결: `timeMin`을 최근 두 달 전 1일로 조정, `maxResults`도 100으로 확대
- 확장: `nextPageToken`을 이용해 페이지 처리를 넣으면 일정이 많아졌을 때 문제가 없다.

---