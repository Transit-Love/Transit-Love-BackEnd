# OAuth2 `authorization_request_not_found` 에러 해결 완료

## ✅ 수정 완료 사항

### 1. SecurityConfig.java 수정
- **세션 정책 변경**: `STATELESS` → `IF_REQUIRED`
- **리다이렉션 엔드포인트 명시**: `/login/oauth2/code/*`
- **위치**: `src/main/java/com/example/transitlove/config/SecurityConfig.java`

### 2. application.properties 수정
- **디버그 로깅 추가**: OAuth2 및 Security 관련 로그
- **위치**: `src/main/resources/application.properties`

### 3. 빌드 성공
- ✅ `./gradlew clean build` 완료
- ✅ 컴파일 에러 없음

---

## ⚠️ 다음 단계 (필수)

### 1. Google Cloud Console 리다이렉트 URI 추가

[Google Cloud Console](https://console.cloud.google.com/)에 접속하여:

**경로**: API 및 서비스 > 사용자 인증 정보 > OAuth 2.0 클라이언트 ID 편집

**추가할 URI**:
```
http://localhost:8080/login/oauth2/code/google
```

📄 **자세한 가이드**: `GOOGLE_CONSOLE_SETUP_GUIDE.md` 참고

### 2. 서버 재시작

```bash
cd /Users/hanjiwon/Desktop/transitLove/BackEnd/transitLove
./gradlew bootRun
```

또는 실행 중인 서버가 있다면 재시작하세요.

### 3. 프론트엔드에서 테스트

1. 프론트엔드 앱 접속 (http://localhost:5173)
2. Google 로그인 버튼 클릭
3. Chrome DevTools 열기:
   - **Console 탭**: 에러 메시지 확인
   - **Network 탭**: OAuth 요청 흐름 확인
   - **Application > Cookies**: JSESSIONID 쿠키 확인

---

## 📊 변경 사항 상세

### SecurityConfig.java

```java
// 이전
.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
)

// 변경 후
.sessionManagement(session -> session
    // OAuth2 로그인을 위해 세션 생성 허용
    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
)
```

```java
// 추가됨
.redirectionEndpoint(redirection -> redirection
    // Google이 콜백하는 백엔드 엔드포인트 명시
    .baseUri("/login/oauth2/code/*")
)
```

### application.properties

```properties
# 추가됨
logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.security.oauth2=DEBUG
logging.level.org.springframework.web.cors=DEBUG
```

---

## 🔍 문제 원인 분석

### `authorization_request_not_found` 에러 발생 이유:

1. **세션 정책 문제**:
   - `STATELESS` 정책으로 인해 OAuth2 인증 요청 시 생성된 authorization request가 세션에 저장되지 않음
   - Google 콜백 시 해당 authorization request를 찾을 수 없어 에러 발생

2. **OAuth2 인증 흐름**:
   ```
   1. /oauth2/authorize/google 요청
      → Spring Security가 authorization request를 세션에 저장 (필요!)
   
   2. Google 로그인 페이지로 리다이렉트
   
   3. /login/oauth2/code/google?code=xxx 콜백
      → 세션에서 authorization request 조회 (실패했었음)
      → authorization_request_not_found 에러
   ```

### 해결 방법:

- `SessionCreationPolicy.IF_REQUIRED` 사용
  - OAuth2 인증 시에만 세션 생성
  - 일반 API 요청은 여전히 JWT 기반 인증 사용
  - 보안 문제 없음

---

## 🧪 테스트 시나리오

### 예상 동작:

#### 1단계: 로그인 버튼 클릭
```
요청: GET http://localhost:8080/oauth2/authorize/google
응답: 302 Redirect → https://accounts.google.com/o/oauth2/v2/auth?...
쿠키: JSESSIONID=xxx (백엔드에서 설정)
```

#### 2단계: Google 로그인
```
사용자가 Google 계정으로 로그인 및 권한 승인
```

#### 3단계: 백엔드 콜백
```
요청: GET http://localhost:8080/login/oauth2/code/google?code=xxx
쿠키: JSESSIONID=xxx (이전 단계에서 받은 쿠키)
처리: Spring Security가 authorization request 조회 성공 ✅
```

#### 4단계: 프론트엔드 리다이렉트
```
응답: 302 Redirect → http://localhost:5173/oauth2/redirect?token=xxx&role=xxx
결과: 로그인 성공, JWT 토큰 발급
```

---

## 📚 생성된 문서

1. **OAUTH2_ERROR_FIX_REPORT.md**: 전체 수정 내역 및 상세 설명
2. **GOOGLE_CONSOLE_SETUP_GUIDE.md**: Google Cloud Console 설정 가이드
3. **QUICK_START.md** (이 파일): 빠른 시작 가이드

---

## 🐛 문제가 지속될 경우

### 체크리스트:

- [ ] Google Cloud Console에 `http://localhost:8080/login/oauth2/code/google` 추가했는지 확인
- [ ] 서버를 재시작했는지 확인
- [ ] Chrome DevTools에서 Network 탭 확인
- [ ] 백엔드 로그 확인 (`tail -f server.log`)
- [ ] 쿠키가 제대로 전송되는지 확인 (Application > Cookies)

### 로그 확인 명령어:

```bash
# OAuth2 관련 로그만 필터링
tail -f server.log | grep -E "oauth|OAuth|OAUTH"

# Spring Security 로그 확인
tail -f server.log | grep -E "security|Security"

# 세션 관련 로그 확인
tail -f server.log | grep -E "session|Session"
```

### 추가 디버깅:

1. **백엔드 로그에서 확인할 내용**:
   ```
   DEBUG o.s.security.oauth2.client - Authorization Request: ...
   DEBUG o.s.security.web.savedrequest - Saved request ... to session
   DEBUG o.s.security.oauth2.client - Successfully authenticated user
   ```

2. **Network 탭에서 확인할 내용**:
   - `/oauth2/authorize/google`: Set-Cookie 헤더에 JSESSIONID 있는지
   - `/login/oauth2/code/google`: Cookie 헤더에 JSESSIONID 포함되는지

3. **Cookies 탭에서 확인할 내용**:
   - `localhost:8080` 도메인에 `JSESSIONID` 쿠키 존재하는지
   - `SameSite` 속성 확인

---

## 📞 지원

문제가 계속되면 다음 정보와 함께 문의하세요:

1. 백엔드 로그 (OAuth2 관련 부분)
2. Chrome DevTools Network 탭 스크린샷
3. Chrome DevTools Console 에러 메시지
4. Google Cloud Console 리다이렉트 URI 설정 스크린샷

---

## ✨ 성공 시 예상 결과

```
✅ Google 로그인 버튼 클릭
✅ Google 로그인 페이지로 이동
✅ 계정 선택 및 권한 승인
✅ 프론트엔드로 자동 리다이렉트
✅ JWT 토큰 저장
✅ 메인 페이지로 이동
✅ 로그인 완료!
```

---

**다음 단계**: Google Cloud Console에서 리다이렉트 URI를 추가하고 서버를 재시작하세요! 🚀

