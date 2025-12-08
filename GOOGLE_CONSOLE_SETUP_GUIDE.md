# Google Cloud Console OAuth2 설정 가이드

## 중요: 반드시 확인 필요 ⚠️

현재 `authorization_request_not_found` 에러를 해결하기 위해 백엔드 설정을 수정했습니다.
마지막 단계로 **Google Cloud Console**에서 리다이렉트 URI를 확인해야 합니다.

## 1. Google Cloud Console 접속

1. [Google Cloud Console](https://console.cloud.google.com/) 접속
2. 프로젝트 선택 (Transit Love 프로젝트)
3. 좌측 메뉴: **API 및 서비스** > **사용자 인증 정보** 클릭

## 2. OAuth 2.0 클라이언트 ID 찾기

현재 사용 중인 Client ID:
```
1041970160070-12pgjprddbtull2db9r80f6g6c9cl1h8.apps.googleusercontent.com
```

위 Client ID를 찾아서 클릭 (편집 화면으로 이동)

## 3. 승인된 리디렉션 URI 확인 및 추가

### 반드시 포함되어야 할 URI:

#### 로컬 개발 환경:
```
http://localhost:8080/login/oauth2/code/google
```

#### 프론트엔드 리다이렉트 (선택사항):
```
http://localhost:5173/oauth2/redirect
```

### 스크린샷 예시:

```
┌────────────────────────────────────────────────────────────┐
│ 승인된 리디렉션 URI                                         │
├────────────────────────────────────────────────────────────┤
│ 1  http://localhost:8080/login/oauth2/code/google      [X] │
│ 2  http://localhost:5173/oauth2/redirect               [X] │
│                                                              │
│ [+ URI 추가]                                                │
└────────────────────────────────────────────────────────────┘
```

## 4. URI 추가 방법

1. **"+ URI 추가"** 버튼 클릭
2. 다음 URI 입력:
   ```
   http://localhost:8080/login/oauth2/code/google
   ```
3. **"저장"** 버튼 클릭

⚠️ **주의사항:**
- URI는 **대소문자를 구분**합니다
- 마지막에 `/`를 포함하지 마세요
- `http://` (로컬 개발) 또는 `https://` (프로덕션)를 정확히 입력하세요

## 5. 설정 확인 체크리스트

- [ ] Google Cloud Console에 로그인
- [ ] Transit Love 프로젝트 선택
- [ ] API 및 서비스 > 사용자 인증 정보 페이지 접속
- [ ] OAuth 2.0 클라이언트 ID 찾기 (1041970160070-...)
- [ ] 승인된 리디렉션 URI 섹션 확인
- [ ] `http://localhost:8080/login/oauth2/code/google` 추가
- [ ] 저장 버튼 클릭
- [ ] 백엔드 서버 재시작
- [ ] 프론트엔드에서 로그인 테스트

## 6. OAuth2 흐름 이해

### 백엔드 콜백 URI (필수):
```
http://localhost:8080/login/oauth2/code/google
```
- **역할**: Google이 인증 완료 후 **백엔드로 직접** 호출하는 URI
- **처리**: Spring Security가 자동으로 처리
- **결과**: 사용자 정보 가져오기 + JWT 토큰 생성

### 프론트엔드 리다이렉트 URI (최종):
```
http://localhost:5173/oauth2/redirect
```
- **역할**: 백엔드가 로그인 완료 후 **프론트엔드로** 리다이렉트하는 URI
- **처리**: 프론트엔드 React 코드에서 처리
- **결과**: JWT 토큰 저장 + 메인 페이지 이동

## 7. 프로덕션 환경 ���정 (향후)

프로덕션 배포 시 추가로 등록해야 할 URI:

### 백엔드 콜백:
```
https://your-backend-domain.com/login/oauth2/code/google
```

### 프론트엔드 리다이렉트:
```
https://your-frontend-domain.com/oauth2/redirect
```

그리고 `.env` 파일 또는 환경 변수 업데이트:
```env
OAUTH2_REDIRECT_URI=https://your-frontend-domain.com/oauth2/redirect
```

## 8. 자주 발생하는 문제

### 문제 1: redirect_uri_mismatch
```
Error 400: redirect_uri_mismatch
```

**원인**: Google Console에 등록된 URI와 요청의 URI가 다름

**해결**:
1. Chrome DevTools > Network 탭에서 실제 사용된 `redirect_uri` 확인
2. Google Console에 정확히 동일한 URI 등록
3. 대소문자, `/` 등 정확히 일치해야 함

### 문제 2: authorization_request_not_found
```
[authorization_request_not_found]
```

**원인**: 세션이 유지되지 않음

**해결**: ✅ 이미 백엔드에서 수정 완료 (`IF_REQUIRED`)

### 문제 3: access_denied
```
error=access_denied
```

**원인**: 사용자가 Google 로그인 화면에서 "취소" 클릭

**해결**: 정상적인 동작, 에러 핸들러에서 처리됨

## 9. 테스트 시나리오

### 시나리오 1: 정상 로그인
1. 프론트엔드 `/login` 페이지 접속
2. "Google 로그인" 버튼 클릭
3. Google 로그인 페이지로 리다이렉트
4. Google 계정 선택 및 권한 승인
5. `http://localhost:5173/oauth2/redirect?token=xxx` 로 리다이렉트
6. 메인 페이지로 이동 (로그인 완료)

### 시나리오 2: 에러 발생 시
1. Chrome DevTools > Console 탭 확인
2. Network 탭에서 실패한 요청 확인
3. 백엔드 로그 확인 (`tail -f server.log`)
4. 이 문서의 "문제 해결" 섹션 참고

## 10. 백엔드 로그 확인

서버 시작 후 다음 로그가 나타나야 합니다:

```
DEBUG o.s.security.oauth2.client - 
  Registration: google
  Authorization URI: https://accounts.google.com/o/oauth2/v2/auth
  Redirect URI: http://localhost:8080/login/oauth2/code/google
```

OAuth2 로그인 시도 시:

```
DEBUG o.s.security.web.FilterChainProxy - 
  Securing GET /oauth2/authorize/google
  
DEBUG o.s.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter -
  Authorization Request: ...
  
DEBUG o.s.security.web.savedrequest.HttpSessionRequestCache -
  Saved request http://localhost:8080/oauth2/authorize/google to session
```

## 11. 추가 디버깅 도구

### Chrome Extension: EditThisCookie
쿠키를 쉽게 확인하고 수정할 수 있는 확장 프로그램

### Postman
직접 API 테스트 시 유용

### Google OAuth 2.0 Playground
https://developers.google.com/oauthplayground/
Client ID/Secret으로 직접 OAuth 흐름 테스트

## 12. 도움이 필요한 경우

### 백엔드 로그 확인:
```bash
cd /Users/hanjiwon/Desktop/transitLove/BackEnd/transitLove
tail -f server.log | grep -E "oauth|OAuth|OAUTH"
```

### 서버 재시작:
```bash
./gradlew bootRun
```

### 빌드 정리 후 재시작:
```bash
./gradlew clean build
./gradlew bootRun
```

---

## 요약

1. ✅ **백엔드 설정 완료** (`SessionCreationPolicy.IF_REQUIRED`)
2. ⚠️ **Google Console 설정 필요** (리다이렉트 URI 추가)
3. 🔄 **서버 재시작**
4. ✅ **테스트 진행**

**다음 단계**: Google Cloud Console에 접속하여 리다이렉트 URI를 추가하세요!

