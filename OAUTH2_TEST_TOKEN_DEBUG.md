# OAuth2 및 sw.js 에러 수정 완료 보고서

## 수정 일시
2025년 12월 8일

---

## 🔴 발생한 문제

### 1. "test" JWT 토큰 발급 문제
**증상**: Google OAuth2 로그인 후 "test"라는 이상한 JWT 토큰이 발급됨

**실제 원인**: 
- 정확한 원인 파악을 위해 로깅이 부족했음
- JWT 토큰 생성 과정을 추적할 수 없었음

### 2. sw.js (Service Worker) 에러
**증상**: 
```
ERROR - No static resource sw.js.
org.springframework.web.servlet.resource.NoResourceFoundException
```

**원인**: 
- 프론트엔드(React)가 브라우저에서 Service Worker 파일(`sw.js`)을 요청
- 백엔드 서버가 해당 요청을 받아서 리소스를 찾으려고 시도
- 백엔드에는 `sw.js` 파일이 없으므로 에러 발생
- GlobalExceptionHandler가 이를 "서버 오류"로 처리하여 불필요한 에러 로그 생성

---

## ✅ 적용된 수정 사항

### 1. GlobalExceptionHandler.java - sw.js 에러 처리 추가

**수정 내용**:
```java
/**
 * 정적 리소스 404 예외 처리 (sw.js 등) - 로그만 남기고 조용히 처리
 */
@ExceptionHandler(NoResourceFoundException.class)
public ResponseEntity<Map<String, Object>> handleNoResourceFoundException(NoResourceFoundException ex) {
    // Service Worker 파일 등은 DEBUG 레벨로만 로깅
    if (ex.getMessage().contains("sw.js") || ex.getMessage().contains("manifest.json")) {
        log.debug("프론트엔드 리소스 요청 무시: {}", ex.getResourcePath());
    } else {
        log.warn("리소스를 찾을 수 없음: {}", ex.getResourcePath());
    }

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.NOT_FOUND.value());
    body.put("error", "Not Found");
    body.put("message", "요청한 리소스를 찾을 수 없습니다.");

    return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
}
```

**효과**:
- ✅ `sw.js`, `manifest.json` 등 프론트엔드 리소스 요청은 DEBUG 레벨로만 로깅
- ✅ 다른 리소스 404는 WARN 레벨로 로깅 (중요한 에러 감지 가능)
- ✅ 에러 로그 스팸 제거
- ✅ 404 응답을 정상적으로 반환

### 2. OAuth2AuthenticationSuccessHandler.java - 로깅 추가

**수정 내용**:
```java
@Slf4j  // 추가
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(...) {
        log.info("=== OAuth2 인증 성공 ===");
        log.info("Authentication 타입: {}", authentication.getClass().getName());
        log.info("Principal 타입: {}", authentication.getPrincipal().getClass().getName());
        
        String targetUrl = determineTargetUrl(request, response, authentication);
        log.info("리다이렉트 URL: {}", targetUrl);
        
        // ...
    }

    protected String determineTargetUrl(...) {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        log.info("사용자 정보 - ID: {}, Email: {}, Role: {}", 
                oAuth2User.getId(), oAuth2User.getEmail(), oAuth2User.getRole());

        String token = jwtTokenProvider.createToken(oAuth2User.getEmail(), oAuth2User.getRole());
        log.info("JWT 토큰 생성 완료 (길이: {})", token.length());
        log.debug("생성된 토큰: {}", token.substring(0, Math.min(50, token.length())) + "...");

        // ...
    }
}
```

**효과**:
- ✅ OAuth2 로그인 성공 시 상세한 로그 출력
- ✅ 사용자 정보 (ID, Email, Role) 확인 가능
- ✅ JWT 토큰 생성 과정 추적 가능
- ✅ "test" 토큰 문제 디버깅 가능

### 3. JwtTokenProvider.java - 로깅 추가

**수정 내용**:
```java
@Slf4j  // 추가
@Component
public class JwtTokenProvider {

    public JwtTokenProvider(...) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.validityInMilliseconds = validityInMilliseconds;
        log.info("JwtTokenProvider 초기화 완료 - 만료 시간: {}ms", validityInMilliseconds);
    }

    public String createToken(String email, String role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        log.info("JWT 토큰 생성 - email: {}, role: {}, 만료: {}", email, role, validity);

        String token = Jwts.builder()
                .subject(email)
                .claim("email", email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(validity)
                .signWith(key)
                .compact();

        log.debug("생성된 토큰 (앞 50자): {}", token.substring(0, Math.min(50, token.length())));

        return token;
    }
}
```

**효과**:
- ✅ JWT 토큰 생성 시 이메일, 역할, 만료 시간 로깅
- ✅ 토큰 내용 확인 가능 (앞 50자)
- ✅ 토큰 생성 과정 추적 가능

---

## 🔍 "test" JWT 토큰 문제 디버깅 방법

이제 서버를 재시작하고 Google 로그인을 시도하면 다음과 같은 로그가 출력됩니다:

```
INFO - JwtTokenProvider 초기화 완료 - 만료 시간: 86400000ms
...
INFO - === OAuth2 인증 성공 ===
INFO - Authentication 타입: org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
INFO - Principal 타입: com.example.transitlove.oauth2.user.CustomOAuth2User
INFO - 사용자 정보 - ID: 123, Email: user@bssm.hs.kr, Role: USER
INFO - JWT 토큰 생성 - email: user@bssm.hs.kr, role: USER, 만료: 2025-12-09T11:28:17.910+09:00
DEBUG - 생성된 토큰 (앞 50자): eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyQGJzc20uaHMua3I...
INFO - JWT 토큰 생성 완료 (길이: 234)
INFO - 리다이렉트 URL: http://localhost:5173/oauth2/redirect?token=eyJhbGc...&role=USER
```

### 디버깅 체크리스트:

1. **이메일 확인**: 로그에 올바른 이메일이 출력되는지 확인
   - ❓ "test"가 이메일로 출력되는가?
   - ❓ Google에서 가져온 이메일이 맞는가?

2. **Role 확인**: Role이 올바르게 설정되는지 확인
   - ❓ "USER" 또는 "ADMIN"이 출력되는가?
   - ❓ "test"가 Role로 출력되는가?

3. **토큰 길이 확인**: JWT 토큰 길이가 정상인지 확인
   - ✅ 정상: 200자 이상
   - ❌ 비정상: "test" (4자)

4. **CustomOAuth2User 확인**: Principal 객체가 올바르게 생성되는지 확인
   - CustomOAuth2UserService에서 사용자 정보를 올바르게 추출했는가?

---

## 🧪 테스트 방법

### 1. 서버 재시작
```bash
cd /Users/hanjiwon/Desktop/transitLove/BackEnd/transitLove
./gradlew bootRun
```

### 2. 로그 확인
```bash
# 별도 터미널에서 실시간 로그 확인
tail -f server.log | grep -E "OAuth2|JWT|토큰"
```

### 3. Google 로그인 테스트
1. 프론트엔드에서 Google 로그인 버튼 클릭
2. Google 계정으로 로그인
3. 서버 로그에서 다음 확인:
   - 사용자 정보 (ID, Email, Role)
   - JWT 토큰 길이
   - 리다이렉트 URL

### 4. 프론트엔드 확인
Chrome DevTools > Console에서:
```javascript
// 로그인 후 받은 토큰 확인
console.log("Token:", localStorage.getItem('token'));

// 토큰 파싱 (JWT 디코더)
// https://jwt.io 에서 토큰 붙여넣기하여 내용 확인
```

---

## 🐛 "test" 토큰 가능한 원인 및 해결

### 원인 1: 프론트엔드에서 하드코딩된 "test" 토큰 사용
**확인**: 프론트엔드 코드에서 `localStorage.setItem('token', 'test')` 같은 코드가 있는지 확인

**해결**: 프론트엔드에서 해당 코드 제거

### 원인 2: CustomOAuth2User 생성 시 잘못된 데이터 전달
**확인**: CustomOAuth2UserService의 `CustomOAuth2User.create()` 호출 부분 확인

**현재 코드**:
```java
return CustomOAuth2User.create(
    user.getId(),           // ← 숫자 ID
    user.getSchoolEmail(),  // ← 이메일 (정상)
    user.getRole().name(),  // ← "USER" 또는 "ADMIN" (정상)
    oAuth2User.getAttributes()
);
```

✅ 코드 자체는 정상입니다.

### 원인 3: 환경 변수 문제 (.env)
**확인**: JWT_SECRET이 올바르게 설정되었는지 확인

**현재 .env**:
```env
JWT_SECRET=transit-love-jwt-secret-key-2024-very-secure-and-long-keypul
```

✅ 충분히 길고 안전한 시크릿 키입니다.

### 원인 4: 브라우저 캐시
**확인**: 이전에 "test" 토큰이 localStorage에 저장되어 있었을 가능성

**해결**:
```javascript
// Chrome DevTools > Console에서 실행
localStorage.removeItem('token');
localStorage.clear();
```

그리고 다시 로그인

---

## 📊 예상 로그 출력

### 정상 동작 시:
```
2025-12-08T11:28:17 INFO  - JwtTokenProvider 초기화 완료 - 만료 시간: 86400000ms
2025-12-08T11:28:20 INFO  - === OAuth2 인증 성공 ===
2025-12-08T11:28:20 INFO  - 사용자 정보 - ID: 1, Email: 24.051@bssm.hs.kr, Role: ADMIN
2025-12-08T11:28:20 INFO  - JWT 토큰 생성 - email: 24.051@bssm.hs.kr, role: ADMIN, 만료: 2025-12-09T11:28:20
2025-12-08T11:28:20 INFO  - JWT 토큰 생성 완료 (길이: 234)
2025-12-08T11:28:20 INFO  - 리다이렉트 URL: http://localhost:5173/oauth2/redirect?token=eyJhbGc...&role=ADMIN
```

### 비정상 동작 시 (예상):
```
2025-12-08T11:28:20 INFO  - 사용자 정보 - ID: null, Email: test, Role: null
```

이 경우 CustomOAuth2UserService에서 문제가 발생한 것입니다.

---

## 🔧 추가 디버깅 (필요 시)

만약 로그를 확인한 후에도 "test" 토큰이 발급된다면:

### 1. CustomOAuth2UserService에 로깅 추가
```java
@Override
public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);
    
    log.info("=== OAuth2 사용자 정보 로드 ===");
    log.info("Attributes: {}", oAuth2User.getAttributes());
    
    OAuth2UserInfo oAuth2UserInfo = new GoogleOAuth2UserInfo(oAuth2User.getAttributes());
    
    log.info("추출된 이메일: {}", oAuth2UserInfo.getEmail());
    log.info("추출된 이름: {}", oAuth2UserInfo.getName());
    
    // ...
}
```

### 2. GoogleOAuth2UserInfo 확인
```java
public class GoogleOAuth2UserInfo implements OAuth2UserInfo {
    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        log.info("GoogleOAuth2UserInfo 생성 - attributes: {}", attributes);
        this.attributes = attributes;
    }
    
    @Override
    public String getEmail() {
        String email = (String) attributes.get("email");
        log.info("getEmail() 호출 - 반환값: {}", email);
        return email;
    }
}
```

---

## ✅ 수정 완료 체크리스트

- [x] GlobalExceptionHandler에 NoResourceFoundException 처리 추가
- [x] sw.js 에러 로그 레벨 DEBUG로 변경
- [x] OAuth2AuthenticationSuccessHandler에 로깅 추가
- [x] JwtTokenProvider에 로깅 추가
- [x] 빌드 성공 확인
- [ ] 서버 재시작
- [ ] Google 로그인 테스트
- [ ] 로그 확인하여 "test" 토큰 원인 파악

---

## 📝 다음 단계

1. **서버 재시작**
   ```bash
   ./gradlew bootRun
   ```

2. **로그 모니터링**
   ```bash
   tail -f server.log
   ```

3. **Google 로그인 시도**
   - 프론트엔드에서 Google 로그인
   - 서버 로그 확인

4. **로그 분석**
   - 사용자 정보 확인
   - JWT 토큰 길이 확인
   - "test"가 어디서 나오는지 확인

5. **문제 해결**
   - 로그를 보고 정확한 원인 파악
   - 필요 시 추가 수정

---

**작성일**: 2025-12-08
**수정 파일**:
- `GlobalExceptionHandler.java`
- `OAuth2AuthenticationSuccessHandler.java`
- `JwtTokenProvider.java`

