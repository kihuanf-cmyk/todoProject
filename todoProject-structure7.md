# Google OAuth 로그인 기능 구현 정리

## 1. 개요

기존 Spring Security 폼 로그인(자체 회원가입/로그인)에 **Google OAuth2 로그인**을 추가했다.
폼 로그인과 구글 로그인이 같은 `SecurityFilterChain`에서 공존하며, 로그인 성공 후에는
컨트롤러에서 두 방식 모두 동일한 `CustomUserDetails` 타입으로 다룰 수 있도록 통일했다.

---

## 2. Google Cloud Console 설정

1. Google Cloud Console에서 프로젝트 생성
2. **OAuth 동의 화면** 구성 (User Type: 외부/External, 테스트 사용자에 본인 계정 등록 필요)
3. **사용자 인증 정보 > OAuth 클라이언트 ID** 발급
   - 애플리케이션 유형: 웹 애플리케이션
   - 승인된 리디렉션 URI: `http://localhost:8282/login/oauth2/code/google`
   - 발급된 client-id / client-secret은 환경변수(`GOOGLE_ID`, `GOOGLE_PWD`)로 관리
4. **Calendar API** 활성화 (추후 캘린더 연동을 위해 미리 켜둠)

> Windows 환경변수 등록 후에는 STS(이클립스)를 완전히 재시작해야 새 값이 반영된다.
> (IDE를 환경변수 추가 전에 이미 켜둔 상태였다면 `${GOOGLE_ID}`가 치환되지 않아
> `401 invalid_client` 오류가 발생했던 적이 있음)

---

## 3. 의존성 및 설정

### pom.xml
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

### application.properties
```properties
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_PWD}
spring.security.oauth2.client.registration.google.scope=email,profile
```

---

## 4. 클래스 구조

### CustomOAuth2UserService (구현체)
- `DefaultOAuth2UserService`를 상속해 구글에서 사용자 정보를 가져오는 기본 로직 재사용
- `loadUser()`에서 이메일 기준으로 `TODO_USER` 조회 → 없으면 자동 회원가입(`resolveUser()`로 분리)
- 신규 가입 시 `pwd`는 `UUID.randomUUID()`를 `PasswordEncoder`로 암호화해 저장(실제 로그인에는 사용되지 않음)
- `resolveUser()`는 테스트 패키지(`todoProject`)에서 직접 호출해 단위 테스트하기 위해 `public`으로 열어둠
- 로그: 요청 수신(`debug`), 신규 등록(`info`), 기존 사용자 확인(`debug`)

### CustomOAuth2UserDetails (신규)
`CustomUserDetails`를 상속하면서 `OAuth2User`도 구현.
- `@AuthenticationPrincipal CustomUserDetails`로 폼 로그인/구글 로그인 모두 동일하게 처리 가능하게 하기 위해 도입
- `getAttributes()`는 구글이 내려준 원본 정보(email, name 등) 반환
- `getName()`은 `getUsername()`(= user_id = 이메일)을 그대로 사용

### CustomSecurityConfig
- `filterChain()` 메서드 파라미터로 `CustomOAuth2UserService oAuth2UserService`를 추가로 주입받음
- `formLogin()` 블록 뒤, `rememberMe()` 블록 앞에 `oauth2Login()` 추가
```java
.oauth2Login(oauth2 -> oauth2
    .loginPage("/user/login")
    .defaultSuccessUrl("/todo/list", true)
    .userInfoEndpoint(userInfo -> userInfo
        .userService(oAuth2UserService)
    )
)
```

### 로그인 화면
```html
<a href="/oauth2/authorization/google" class="btn btn-outline-danger w-100 mt-2">
    Google로 로그인
</a>
```
`/oauth2/authorization/google`은 Spring Security가 `registration.google` 설정을 기반으로
자동 생성하는 경로이며, Google Cloud Console에 등록한 URL이 아니다(별도 컨트롤러 불필요).

---

## 5. 겪은 오류와 해결

### 5-1. `401 invalid_client`
- 증상: 구글 로그인 화면에서 "The OAuth client was not found"
- 원인: Windows 환경변수(`GOOGLE_ID`) 등록 후 STS를 재시작하지 않아 `${GOOGLE_ID}`가 치환되지 않음
- 해결: STS 완전 재시작 후 정상 동작 확인

### 5-2. `/todo/list` 이동 시 500 (NullPointerException)
- 증상: 구글 로그인은 성공(TODO_USER에 자동 등록까지 확인)하지만 `/todo/list`에서
  `userDetails.getUser_no()` 호출 시 NPE 발생
- 원인: `CustomOAuth2UserServiceImpl.loadUser()`가 `DefaultOAuth2User` 타입을 반환하고 있어,
  `@AuthenticationPrincipal CustomUserDetails userDetails`에 타입이 맞지 않아 `null`이 주입됨
  (폼 로그인은 `CustomUserDetailsService`가 `CustomUserDetails`를 반환해 문제 없었음)
- 해결: `CustomOAuth2UserDetails`(`CustomUserDetails` + `OAuth2User`)를 새로 만들어
  `loadUser()`가 이를 반환하도록 수정

---

## 6. 테스트 코드

`UserTests.java`(package `todoProject`)에 `GoogleOAuth사용자판별테스트` Nested 클래스 추가.
`resolveUser()`만 별도로 분리해 테스트(= `super.loadUser()`의 실제 네트워크 호출 없이 검증 가능).

- **신규이메일은자동등록된다**: `selectUserById`가 null을 반환하면 `insertUser` 호출,
  `pwd`는 `passwordEncoder.encode()` 결과, `role`은 `UserRole.USER`로 설정되는지 검증
- **기존이메일은재등록하지않는다**: 기존 사용자가 있으면 그대로 반환하고 `insertUser`가
  호출되지 않는지 검증(`verify(userMapper, never()).insertUser(...)`)

---
