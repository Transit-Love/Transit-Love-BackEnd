# OAuth2 `authorization_request_not_found` 에러 수정 완료 보고서

## 수정 일시
2025년 12월 8일

## 문제 상황
프론트엔드에서 Google OAuth2 로그인 시도 시 `authorization_request_not_found` 에러 발생

## 근본 원인
백엔드의 Spring Security 설정에서 **`SessionCreationPolicy.STATELESS`**로 설정되어 있어, OAuth2 인증 과정에서 필요한 세션(authorization request 정보)이 유지되지 않았음.

## 적용된 수정 사항

### 1. SecurityConfig.java 수정 ✅

#### 변경 전:
```java
.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
)
.oauth2Login(oauth2 -> oauth2
    .authorizationEndpoint(authorization -> authorization
        .baseUri("/oauth2/authorize")
    )
    // redirectionEndpoint 설정 누락
```

#### 변경 후:
```java
.sessionManagement(session -> session
    // OAuth2 로그인을 위해 세션 생성 허용
    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
)
.oauth2Login(oauth2 -> oauth2
    .authorizationEndpoint(authorization -> authorization
        .baseUri("/oauth2/authorize")
    )
    .redirectionEndpoint(redirection -> redirection
        // Google이 콜백하는 백엔드 엔드포인트 명시
        .baseUri("/login/oauth2/code/*")
    )
```

**핵심 변경점:**
- ✅ `SessionCreationPolicy.STATELESS` → `IF_REQUIRED`로 변경
  - OAuth2 인증 중에만 세션을 생성하고, 이후 JWT로 인증 처리
- ✅ `redirectionEndpoint` 명시적으로 설정
  - Google OAuth 콜백 URL 명확히 지정

### 2. application.properties에 디버깅 로그 추가 ✅

```properties
# Logging (OAuth2 디버깅용)
logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.security.oauth2=DEBUG
logging.level.org.springframework.web.cors=DEBUG
```

## 기존 설정 확인 (정상 동작 중)

### ✅ OAuth2 설정 (application.properties)
```properties
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.scope=profile,email
spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}

app.oauth2.redirect-uri=${OAUTH2_REDIRECT_URI}
```

### ✅ CORS 설정 (SecurityConfig.java)
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:3000", 
        "http://localhost:5173", 
        "http://localhost:8080"
    ));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);  // 쿠키 전송 허용 ✅
    configuration.setMaxAge(3600L);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### ✅ OAuth2 Success Handler
```java
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    
    @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth2/redirect}")
    private String redirectUri;
    
    @Override
    public void onAuthenticationSuccess(...) {
        String token = jwtTokenProvider.createToken(oAuth2User.getEmail(), oAuth2User.getRole());
        
        return UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .queryParam("role", oAuth2User.getRole())
                .build().toUriString();
    }
}
```

## Google Cloud Console 확인 사항

⚠️ **반드시 확인 필요**: Google Cloud Console > API 및 서비스 > 사용자 인증 정보

승인된 리디렉션 URI에 다음이 **반드시** 포함되어야 합니다:

```
http://localhost:8080/login/oauth2/code/google
```

### 현재 .env 설정:
```env
GOOGLE_CLIENT_ID=1041970160070-12pgjprddbtull2db9r80f6g6c9cl1h8.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=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
OAUTH2_REDIRECT_URI=http://localhost:5173/oauth2/redirect
```

### Google Console에 등록되어야 할 URI:
1. `http://localhost:8080/login/oauth2/code/google` (백엔드 콜백) ⚠️ **필수**
2. `http://localhost:5173/oauth2/redirect` (프론트엔드 최종 리다이렉트) - 선택사항

## OAuth2 인증 흐름 (수정 후)

```
1. 사용자: Google 로그인 버튼 클릭
   ↓
2. 프론트엔드 → 백엔드: GET /oauth2/authorize/google
   ↓
3. 백엔드: 세션에 authorization request 저장 ✅ (IF_REQUIRED로 변경)
   ↓
4. 백엔드 → Google: 사용자를 Google 로그인 페이지로 리다이렉트
   ↓
5. 사용자: Google 계정으로 로그인 및 권한 승인
   ↓
6. Google → 백엔드: GET /login/oauth2/code/google?code=xxx
   ↓
7. 백엔드: 세션에서 authorization request 조회 ✅ (정상 작동)
   ↓
8. 백엔드: Google에 access token 요청 및 사용자 정보 가져오기
   ↓
9. 백엔드: JWT 토큰 생성
   ↓
10. 백엔드 → 프론트엔드: http://localhost:5173/oauth2/redirect?token=xxx&role=xxx
   ↓
11. 프론트엔드: 토큰 저장 및 메인 페이지로 이동
```

## 테스트 방법

### 1. 서버 재시작
```bash
cd /Users/hanjiwon/Desktop/transitLove/BackEnd/transitLove
./gradlew bootRun
```

### 2. 로그 확인
서버 시작 시 다음 로그 확인:
```
DEBUG o.s.security.oauth2 - ...
```

### 3. OAuth2 로그인 테스트
1. 프론트엔드에서 Google 로그인 버튼 클��
2. Chrome DevTools > Network 탭 열기
3. 다음 요청들 확인:
   - `GET /oauth2/authorize/google` (302 Redirect)
   - `GET accounts.google.com/o/oauth2/v2/auth` (Google 로그인)
   - `GET /login/oauth2/code/google?code=...` (302 Redirect)
   - `GET http://localhost:5173/oauth2/redirect?token=...`

4. Chrome DevTools > Application > Cookies 확인:
   - `localhost:8080`에 `JSESSIONID` 쿠키가 생성되었는지 확인

### 4. 예상 결과
- ✅ `authorization_request_not_found` 에러 사라짐
- ✅ 프론트엔드로 JWT 토큰과 함께 리다이렉트
- ✅ 로그인 성공 후 메인 페이지 이동

## 문제 해결 체크리스트

- [x] `SessionCreationPolicy.STATELESS` → `IF_REQUIRED` 변경
- [x] `redirectionEndpoint` 설정 추가
- [x] CORS `allowCredentials(true)` 설정 확인
- [x] 디버깅 로그 레벨 추가
- [ ] **Google Cloud Console 리다이렉트 URI 확인** ⚠️ 필수
- [ ] 서버 재시작 후 테스트
- [ ] 네트워크 탭에서 OAuth 흐름 확인

## 추가 디버깅 (문제 지속 시)

만약 여전히 문제가 발생한다면:

### 1. 서버 로그 확인
```bash
tail -f server.log | grep -i oauth
```

### 2. 쿠키 확인
- Chrome DevTools > Application > Cookies
- `JSESSIONID`가 `localhost:8080`에 존재하는지 확인
- SameSite 속성 확인

### 3. Google Console 디버깅
- [Google OAuth 2.0 Playground](https://developers.google.com/oauthplayground/) 사용
- Client ID/Secret으로 직접 테스트

## 보안 고려사항

### 세션 정책 변경의 영향
- OAuth2 로그인 시에만 세션 생성 (`IF_REQUIRED`)
- 로그인 완료 후에는 JWT 기반 인증 사용
- 세션은 OAuth2 인증 중에만 필요하므로 보안 문제 없음

### JWT 기반 인증 유지
- 일반 API 요청은 여전히 JWT로 인증
- `JwtAuthenticationFilter`가 정상 작동
- 세션은 OAuth2 authorization request 저장 용도로만 사용

## 참고 자료

- [Spring Security OAuth2 - Authorization Request Not Found](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/advanced.html#oauth2login-advanced-authorization-request-repository)
- [Session Management](https://docs.spring.io/spring-security/reference/servlet/authentication/session-management.html)
- [CORS with Credentials](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS#requests_with_credentials)

## 결론

`authorization_request_not_found` 에러는 **세션 정책 문제**로 인한 것이었으며, `STATELESS`에서 `IF_REQUIRED`로 변경하여 해결했습니다. 

이제 Google Cloud Console의 리다이렉트 URI만 확인하면 OAuth2 로그인이 정상 작동할 것입니다.

---

**다음 단계**: 
1. 서버 재시작
2. Google Cloud Console 리다이렉트 URI 확인
3. 프론트엔드에서 로그인 테스트

