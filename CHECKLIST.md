# OAuth2 설정 완료 체크리스트

## ✅ 완료된 작업

### 백엔드 수정 (2025-12-08)

- [x] `SecurityConfig.java` 수정
  - [x] `SessionCreationPolicy.STATELESS` → `IF_REQUIRED` 변경
  - [x] `redirectionEndpoint` 설정 추가 (`/login/oauth2/code/*`)
  - [x] 주석 추가 (authorization_request_not_found 에러 방지)

- [x] `application.properties` 수정
  - [x] OAuth2 디버그 로깅 추가
  - [x] Spring Security 디버그 로깅 추가
  - [x] CORS 디버그 로깅 추가

- [x] 프로젝트 빌드
  - [x] `./gradlew clean build` 성공
  - [x] 컴파일 에러 없음

- [x] 문서 작성
  - [x] `OAUTH2_ERROR_FIX_REPORT.md` - 상세 수정 보고서
  - [x] `GOOGLE_CONSOLE_SETUP_GUIDE.md` - Google Console 설정 가이드
  - [x] `QUICK_START.md` - 빠른 시작 가이드
  - [x] `CHECKLIST.md` - 이 체크리스트

---

## ⚠️ 남은 작업 (필수)

### 1. Google Cloud Console 설정

- [ ] [Google Cloud Console](https://console.cloud.google.com/) 접속
- [ ] Transit Love 프로젝트 선택
- [ ] **API 및 서비스** > **사용자 인증 정보** 이동
- [ ] OAuth 2.0 클라이언트 ID 찾기
  ```
  Client ID: 1041970160070-12pgjprddbtull2db9r80f6g6c9cl1h8.apps.googleusercontent.com
  ```
- [ ] **승인된 리다이렉션 URI** 섹션에 다음 추가:
  ```
  http://localhost:8080/login/oauth2/code/google
  ```
- [ ] **저장** 버튼 클릭

📄 **상세 가이드**: `GOOGLE_CONSOLE_SETUP_GUIDE.md` 참고

### 2. 서버 재시작

- [ ] 터미널에서 실행:
  ```bash
  cd /Users/hanjiwon/Desktop/transitLove/BackEnd/transitLove
  ./gradlew bootRun
  ```

- [ ] 서버 로그 확인:
  ```
  DEBUG o.s.security.oauth2 - ...
  ```

### 3. 프론트엔드 테스트

- [ ] 프론트엔드 앱 시작 (http://localhost:5173)
- [ ] Chrome DevTools 열기 (F12)
- [ ] **Console 탭** 열어두기
- [ ] **Network 탭** 열어두기
- [ ] **Application > Cookies** 확인 준비

### 4. Google 로그인 테스트

- [ ] "Google 로그인" 버튼 클릭
- [ ] Google 로그인 페이지로 리다이렉트 확인
- [ ] Google 계정 선택
- [ ] 권한 승인
- [ ] 프론트엔드로 자동 리다이렉트 확인
- [ ] JWT 토큰 수신 확인
- [ ] 메인 페이지로 이동 확인

### 5. 네트워크 요청 확인

Chrome DevTools > Network 탭에서 다음 요청들이 보여야 합니다:

- [ ] `GET /oauth2/authorize/google` (Status: 302)
  - [ ] Response Headers에 `Set-Cookie: JSESSIONID=...` 존재
  
- [ ] `GET accounts.google.com/o/oauth2/v2/auth` (Google 로그인)
  
- [ ] `GET /login/oauth2/code/google?code=...` (Status: 302)
  - [ ] Request Headers에 `Cookie: JSESSIONID=...` 포함
  
- [ ] `GET http://localhost:5173/oauth2/redirect?token=...&role=...`

### 6. 쿠키 확인

Chrome DevTools > Application > Cookies에서:

- [ ] `localhost:8080` 도메인에 `JSESSIONID` 쿠키 존재
- [ ] `localhost:5173` 도메인에 JWT 토큰 저장 (localStorage 또는 cookie)

---

## 🐛 문제 해결

### 문제 1: 여전히 `authorization_request_not_found` 에러

**확인 사항**:
- [ ] 서버를 재시작했는지?
- [ ] 브라우저 캐시와 쿠키를 삭제했는지?
- [ ] `JSESSIONID` 쿠키가 생성되는지 확인

**해결 방법**:
```bash
# 서버 재시작
./gradlew bootRun

# 브라우저: Shift + Cmd + Delete (쿠키 삭제)
```

### 문제 2: `redirect_uri_mismatch`

**에러 메시지**:
```
Error 400: redirect_uri_mismatch
```

**원인**: Google Console에 등록된 URI와 실제 요청의 URI가 다름

**해결**:
- [ ] Network 탭에서 실제 사용된 `redirect_uri` 확인
- [ ] Google Console에 정확히 동일한 URI 등록
- [ ] 대소문자, 슬래시 등 정확히 일치하는지 확인

### 문제 3: CORS 에러

**에러 메시지**:
```
Access to XMLHttpRequest ... has been blocked by CORS policy
```

**해결**:
- [ ] `SecurityConfig.java`의 CORS 설정 확인
- [ ] `allowCredentials(true)` 설정 확인
- [ ] 프론트엔드 URL이 `allowedOrigins`에 포함되어 있는지 확인

---

## 📊 예상 결과

### 성공 시나리오:

```
✅ 1단계: Google 로그인 버튼 클릭
   → /oauth2/authorize/google 요청
   → JSESSIONID 쿠키 생성

✅ 2단계: Google 로그인 페이지
   → accounts.google.com으로 리다이렉트
   → 계정 선택 및 권한 승인

✅ 3단계: 백엔드 콜백
   → /login/oauth2/code/google?code=xxx
   → 세션에서 authorization request 조회 성공
   → 사용자 정보 가져오기
   → JWT 토큰 생성

✅ 4단계: 프론트엔드 리다이렉트
   → http://localhost:5173/oauth2/redirect?token=xxx
   → JWT 토큰 저장
   → 메인 페이지로 이동

✅ 로그인 완료!
```

### 실패 시 확인:

**백엔드 로그 확인**:
```bash
tail -f server.log | grep -E "oauth|OAuth|error|Error"
```

**프론트엔드 콘솔 확인**:
- Chrome DevTools > Console 탭
- 빨간색 에러 메시지 확인

---

## 📝 테스트 기록

### 테스트 1: Google 로그인

- **날짜**: ___________
- **테스터**: ___________
- **결과**: [ ] 성공 / [ ] 실패
- **문제점**: ___________
- **해결 방법**: ___________

### 테스트 2: JWT 토큰 발급

- **날짜**: ___________
- **테스터**: ___________
- **결과**: [ ] 성공 / [ ] 실패
- **토큰**: ___________
- **Role**: ___________

### 테스트 3: 인증 필요 API 호출

- **날짜**: ___________
- **API**: /api/matching/auto-match
- **결과**: [ ] 성공 / [ ] 실패
- **응답**: ___________

---

## 🔄 롤백 방법 (문제 발생 시)

만약 새로운 설정에 문제가 있다면 다음과 같이 롤백:

### SecurityConfig.java

```java
// 다시 STATELESS로 변경
.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
)

// redirectionEndpoint 제거
.oauth2Login(oauth2 -> oauth2
    .authorizationEndpoint(authorization -> authorization
        .baseUri("/oauth2/authorize")
    )
    .userInfoEndpoint(userInfo -> userInfo
        .userService(customOAuth2UserService)
    )
    .successHandler(oAuth2AuthenticationSuccessHandler)
    .failureHandler(oAuth2AuthenticationFailureHandler)
);
```

### application.properties

```properties
# 로깅 레벨 제거 또는 INFO로 변경
logging.level.org.springframework.security=INFO
```

---

## 📚 참고 문서

프로젝트 내 생성된 문서:

1. **OAUTH2_ERROR_FIX_REPORT.md**
   - 전체 수정 내역
   - 상세 기술 설명
   - OAuth2 흐름 다이어그램

2. **GOOGLE_CONSOLE_SETUP_GUIDE.md**
   - Google Cloud Console 설정 단계별 가이드
   - 스크린샷 및 예시
   - 프로덕션 환경 설정 방법

3. **QUICK_START.md**
   - 빠른 시작 가이드
   - 트러블슈팅
   - 디버깅 명령어

4. **CHECKLIST.md** (이 파일)
   - 작업 체크리스트
   - 테스트 시나리오
   - 문제 해결 가이드

---

## 🎯 최종 목표

- [ ] `authorization_request_not_found` 에러 해결
- [ ] Google OAuth2 로그인 정상 작동
- [ ] JWT 토큰 발급 정상 작동
- [ ] 프론트엔드-백엔드 인증 흐름 완성
- [ ] 사용자 로그인 → 매칭 시스템 사용 가능

---

**현재 상태**: ✅ 백엔드 수정 완료, ⚠️ Google Console 설정 필요

**다음 단계**: Google Cloud Console 리다이렉트 URI 추가 → 서버 재시작 → 테스트

---

**작성일**: 2025-12-08
**최종 업데이트**: 2025-12-08

