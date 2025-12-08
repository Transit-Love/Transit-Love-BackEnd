# 🚨 OAuth2 "authorization_request_not_found" 완벽 해결 가이드

## 현재 상황
- ✅ 채팅 목록은 정상 작동
- ❌ JWT 토큰은 test1의 정보
- ❌ 프로필도 test1의 정보
- ❌ OAuth2 로그인 시 `authorization_request_not_found` 에러

## 문제 원인
**프론트엔드 localStorage에 test1의 JWT 토큰이 저장되어 있어서, 모든 API 요청에 test1의 토큰이 포함됨**

---

## 🔧 해결 방법 (단계별)

### 1단계: 서버 시작

터미널에서 실행:
```bash
cd /Users/hanjiwon/Desktop/transitLove/BackEnd/transitLove
./start-server-fixed.sh
```

또는:
```bash
cd /Users/hanjiwon/Desktop/transitLove/BackEnd/transitLove
export JAVA_HOME=/Users/hanjiwon/Library/Java/JavaVirtualMachines/corretto-17.0.16/Contents/Home
./gradlew bootRun
```

**서버가 시작되면 다음 메시지가 나타나야 합니다:**
```
Started TransitLoveApplication in X.XXX seconds
```

---

### 2단계: OAuth2 디버깅 페이지 접속

브라우저에서 다음 URL 열기:
```
http://localhost:8080/oauth2-debug.html
```

이 페이지에서 다음을 확인:
1. ✅ 서버 상태: 온라인
2. ❌ 저장된 토큰: test1의 토큰이 있을 것
3. 🔴 **"모든 토큰 삭제" 버튼 클릭**

---

### 3단계: 프론트엔드 캐시 완전 삭제

#### 방법 1: 디버깅 페이지에서
```
http://localhost:8080/oauth2-debug.html
→ "모든 토큰 삭제" 버튼 클릭
```

#### 방법 2: Chrome DevTools에서 (권장)
```javascript
// Chrome DevTools > Console 탭에서 실행

// 1. localStorage 모두 삭제
localStorage.clear();

// 2. sessionStorage 모두 삭제
sessionStorage.clear();

// 3. 쿠키 모두 삭제
document.cookie.split(";").forEach(c => {
    document.cookie = c.replace(/^ +/, "")
        .replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/");
});

// 4. 확인
console.log("LocalStorage:", Object.keys(localStorage).length);
console.log("SessionStorage:", Object.keys(sessionStorage).length);

// 5. 페이지 새로고침
location.reload();
```

#### 방법 3: 브라우저 설정에서
1. Chrome 설정 > 개인정보 및 보안 > 인터넷 사용 기록 삭제
2. "쿠키 및 기타 사이트 데이터" 체크
3. "캐시된 이미지 및 파일" 체크
4. 시간 범위: "전체 기간"
5. "데이터 삭제" 클릭

---

### 4단계: Google Cloud Console 설정 확인

[Google Cloud Console](https://console.cloud.google.com/) 접속:

1. **프로젝트 선택**
2. **API 및 서비스 > 사용자 인증 정보**
3. OAuth 2.0 클라이언트 ID 찾기:
   ```
   1041970160070-12pgjprddbtull2db9r80f6g6c9cl1h8.apps.googleusercontent.com
   ```
4. **승인된 리디렉션 URI 확인**:
   ```
   http://localhost:8080/login/oauth2/code/google
   ```
   이 URI가 **반드시** 있어야 합니다!

5. 없다면 **추가 후 저장**

---

### 5단계: Google 로그인 테스트

#### 옵션 A: 디버깅 페이지에서
```
http://localhost:8080/oauth2-debug.html
→ "Google 로그인 시작" 버튼 클릭
```

#### 옵션 B: 프론트엔드 앱에서
```
http://localhost:5173/login
→ "Google 로그인" 버튼 클릭
```

---

### 6단계: 로그 확인

터미널에서 실시간 로그 확인:
```bash
tail -f /Users/hanjiwon/Desktop/transitLove/BackEnd/transitLove/server.log | grep -E "OAuth2|JWT|토큰"
```

**정상 로그 예시:**
```
DEBUG - OAuth2 경로 감지, JWT 필터 제외: /oauth2/authorize/google
DEBUG - OAuth2 경로 감지, JWT 필터 제외: /login/oauth2/code/google
INFO  - === OAuth2 인증 성공 ===
INFO  - 사용자 정보 - ID: 1, Email: your-email@bssm.hs.kr, Role: USER
INFO  - JWT 토큰 생성 - email: your-email@bssm.hs.kr, role: USER
INFO  - 리다이렉트 URL: http://localhost:5173/oauth2/redirect?token=eyJhbGc...
```

**비정상 로그 (test1 나오면 안 됨!):**
```
❌ INFO  - 사용자 정보 - Email: test1@example.com
```

---

## 🔍 문제 지속 시 체크리스트

### 체크 1: localStorage 정말 비었나?
```javascript
// Chrome DevTools > Console
console.log("Token:", localStorage.getItem('token'));
console.log("AccessToken:", localStorage.getItem('accessToken'));
console.log("All keys:", Object.keys(localStorage));
// 모두 null 또는 빈 배열이어야 함
```

### 체크 2: 서버가 실제로 업데이트되었나?
```bash
# 서버 로그에서 확인
grep "JwtAuthenticationFilter" server.log
grep "OAuth2 경로 감지" server.log
# "OAuth2 경로 감지" 메시지가 있어야 함
```

### 체크 3: 프론트엔드가 Authorization 헤더를 보내고 있나?
```
Chrome DevTools > Network 탭
→ /oauth2/authorize/google 요청 클릭
→ Request Headers 확인
→ "Authorization" 헤더가 없어야 함!
```

### 체크 4: 브라우저 캐시 완전 삭제했나?
```
Chrome DevTools > Application 탭
→ Storage > Clear site data 클릭
→ 모든 체크박스 선택
→ "Clear site data" 클릭
```

---

## 🎯 예상 결과

### 성공 시나리오:

```
1. 사용자: "모든 토큰 삭제" 클릭
   ✅ localStorage 비움
   
2. 사용자: "Google 로그인" 클릭
   ✅ /oauth2/authorize/google 요청
   ✅ Authorization 헤더 없음
   ✅ JWT 필터 건너뜀
   
3. Google 로그인 페이지
   ✅ 계정 선택
   ✅ 권한 승인
   
4. /login/oauth2/code/google?code=xxx
   ✅ JWT 필터 건너뜀
   ✅ CustomOAuth2UserService 실행
   ✅ Google 계정 정보로 사용자 생성/업데이트
   
5. OAuth2AuthenticationSuccessHandler
   ✅ Google 계정 이메일로 JWT 토큰 생성
   ✅ http://localhost:5173/oauth2/redirect?token=xxx
   
6. 프론트엔드
   ✅ 토큰 저장
   ✅ 프로필 API 호출 → Google 계정 정보 반환
   ✅ 채팅 목록 API 호출 → 정상 작동
```

---

## 📋 최종 체크리스트

### 백엔드:
- [ ] 서버 실행 중 (포트 8080)
- [ ] JwtAuthenticationFilter 수정 완료 (shouldNotFilter)
- [ ] SecurityConfig IF_REQUIRED 설정 완료
- [ ] 로그에 "OAuth2 경로 감지, JWT 필터 제외" 출력됨

### Google Cloud Console:
- [ ] Client ID: 1041970160070-...
- [ ] 리다이렉트 URI: http://localhost:8080/login/oauth2/code/google
- [ ] 저장 완료

### 프론트엔드:
- [ ] localStorage.clear() 실행
- [ ] sessionStorage.clear() 실행
- [ ] 브라우저 캐시 삭제
- [ ] 페이지 새로고침

### 테스트:
- [ ] http://localhost:8080/oauth2-debug.html 접속
- [ ] 서버 상태: 온라인
- [ ] 저장된 토큰: 없음
- [ ] Google 로그인 클릭
- [ ] 로그인 성공 후 토큰 확인
- [ ] 토큰에 Google 계정 이메일 포함 확인

---

## 🐛 여전히 안 되면?

### 시나리오 1: "authorization_request_not_found" 계속 발생
**원인**: 세션이 유지되지 않음

**해결**:
```bash
# application.properties 확인
grep "session" src/main/resources/application.properties

# IF_REQUIRED로 설정되어 있어야 함
```

### 시나리오 2: test1 토큰이 계속 나옴
**원인**: 프론트엔드가 여전히 오래된 토큰 사용

**해결**:
```javascript
// 강력한 캐시 삭제
window.indexedDB.databases().then(dbs => {
    dbs.forEach(db => window.indexedDB.deleteDatabase(db.name));
});
localStorage.clear();
sessionStorage.clear();
location.reload(true); // 강제 새로고침
```

### 시나리오 3: Google 로그인은 되는데 토큰이 이상함
**원인**: CustomOAuth2UserService 문제

**확인**:
```bash
# 서버 로그 확인
grep "CustomOAuth2UserService" server.log
grep "사용자 정보" server.log
```

---

## 🎬 빠른 해결 (한 줄 정리)

```bash
# 1. 서버 시작
cd /Users/hanjiwon/Desktop/transitLove/BackEnd/transitLove && ./start-server-fixed.sh

# 2. 브라우저에서 실행 (Chrome DevTools > Console)
localStorage.clear(); sessionStorage.clear(); location.reload();

# 3. Google 로그인
http://localhost:8080/oauth2-debug.html → "모든 토큰 삭제" → "Google 로그인 시작"
```

---

## 📞 추가 도움

생성된 파일:
- ✅ `start-server-fixed.sh` - 서버 시작 스크립트
- ✅ `oauth2-debug.html` - 디버깅 도구 (http://localhost:8080/oauth2-debug.html)
- ✅ `JWT_TOKEN_ISSUE_FIX.md` - JWT 토큰 문제 상세 설명
- ✅ `OAUTH2_ERROR_FIX_REPORT.md` - OAuth2 에러 수정 보고서

**작성일**: 2025-12-08

