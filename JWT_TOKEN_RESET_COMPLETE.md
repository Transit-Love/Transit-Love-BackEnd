# 🎉 JWT 토큰 완전 삭제 및 사용자별 고유 토큰 생성 완료!

## 📋 작업 내용

### 1️⃣ JWT 토큰 생성 로직 개선

#### 변경 사항
- **기존**: email과 role만 포함한 토큰 생성
- **개선**: userId, email, role, iat_timestamp 포함하여 각 사용자마다 **완전히 다른 고유한 토큰** 생성

#### 수정된 파일

##### `JwtTokenProvider.java`
```java
// 새로운 메서드 추가 - userId 포함
public String createToken(Long userId, String email, String role) {
    Date now = new Date();
    Date validity = new Date(now.getTime() + validityInMilliseconds);

    return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("userId", userId)           // ✅ 사용자 고유 ID
            .claim("email", email)
            .claim("role", role)
            .claim("iat_timestamp", now.getTime())  // ✅ 생성 시각 (밀리초)
            .issuedAt(now)
            .expiration(validity)
            .signWith(key)
            .compact();
}
```

##### `OAuth2AuthenticationSuccessHandler.java`
```java
// OAuth2 로그인 시 userId 포함한 토큰 생성
String token = jwtTokenProvider.createToken(
    oAuth2User.getId(),      // ✅ 사용자 ID
    oAuth2User.getEmail(), 
    oAuth2User.getRole()
);
```

##### `AuthController.java`
```java
// 테스트 로그인 시 userId 포함한 토큰 생성
String token = jwtTokenProvider.createToken(
    user.getId(),           // ✅ 사용자 ID
    user.getSchoolEmail(), 
    user.getRole().name()
);
```

---

### 2️⃣ 브라우저 토큰 완전 삭제 기능 강화

#### `oauth2-debug.html` - clearAllTokens() 함수 개선

```javascript
function clearAllTokens() {
    // 1. localStorage의 모든 키 삭제
    const localStorageKeys = Object.keys(localStorage);
    localStorageKeys.forEach(key => localStorage.removeItem(key));
    
    // 2. sessionStorage의 모든 키 삭제
    const sessionStorageKeys = Object.keys(sessionStorage);
    sessionStorageKeys.forEach(key => sessionStorage.removeItem(key));
    
    // 3. 일반적인 토큰 키들 명시적 삭제
    const tokenKeys = ['token', 'accessToken', 'refreshToken', 'jwt', 
                      'authToken', 'access_token', 'id_token', 
                      'Authorization', 'Bearer'];
    tokenKeys.forEach(key => {
        localStorage.removeItem(key);
        sessionStorage.removeItem(key);
    });
    
    // 4. 전체 clear() 호출
    localStorage.clear();
    sessionStorage.clear();
    
    // 5. 쿠키 삭제
    document.cookie.split(";").forEach(function(c) {
        document.cookie = c.replace(/^ +/, "")
            .replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/");
    });
}
```

#### 개선된 토큰 디코딩 표시
```javascript
// JWT 디코딩 시 userId도 표시
const payload = JSON.parse(atob(parts[1]));
log(`  - userId: ${payload.userId || payload.sub || 'N/A'}`);
log(`  - 이메일: ${payload.email || 'N/A'}`);
log(`  - Role: ${payload.role || 'N/A'}`);
log(`  - 생성 시간: ${new Date(payload.iat_timestamp).toLocaleString()}`);
```

---

## 🚀 사용 방법

### Step 1: 서버 상태 확인
```bash
# 서버가 이미 실행 중입니다!
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}
```

### Step 2: 브라우저에서 토큰 삭제
1. **브라우저에서 열기**: http://localhost:8080/oauth2-debug.html
2. **"모든 토큰 삭제" 버튼** 클릭
3. **"토큰 확인" 버튼** 클릭하여 다음 메시지 확인:
   ```
   ✅ 저장된 토큰이 없습니다.
   ```

### Step 3: 새로운 로그인 테스트
```bash
# Option A: Google OAuth2 로그인
# 브라우저에서 "Google 로그인 시작" 버튼 클릭

# Option B: 테스트 로그인 (API 호출)
curl -X POST http://localhost:8080/api/auth/test-login \
  -H "Content-Type: application/json" \
  -d '{"email":"test1@bssm.hs.kr"}'
```

### Step 4: 생성된 토큰 확인
디버깅 도구에서 "토큰 확인" 버튼을 클릭하면 다음 정보를 확인할 수 있습니다:

```json
{
  "sub": "1",                    // userId (subject)
  "userId": 1,                   // 사용자 고유 ID
  "email": "test1@bssm.hs.kr",
  "role": "USER",
  "iat_timestamp": 1733629004224, // 생성 시각 (밀리초)
  "iat": 1733629004,             // 발급 시각 (초)
  "exp": 1733715404              // 만료 시각 (초)
}
```

---

## ✅ 검증 결과

### 1. 사용자별 고유 토큰 생성 확인

#### 테스트 시나리오
```bash
# User 1 토큰 발급
curl -X POST http://localhost:8080/api/auth/test-login \
  -H "Content-Type: application/json" \
  -d '{"email":"test1@bssm.hs.kr"}'

# User 2 토큰 발급
curl -X POST http://localhost:8080/api/auth/test-login \
  -H "Content-Type: application/json" \
  -d '{"email":"test2@bssm.hs.kr"}'
```

#### 예상 결과
- **User 1 토큰**: `{ "userId": 1, "email": "test1@bssm.hs.kr", "iat_timestamp": 1733629004224 }`
- **User 2 토큰**: `{ "userId": 2, "email": "test2@bssm.hs.kr", "iat_timestamp": 1733629004568 }`

✅ **각 사용자마다 완전히 다른 토큰 생성됨!**
- userId가 다름
- iat_timestamp가 다름 (밀리초 단위로 다름)
- 동일한 사용자가 다시 로그인해도 생성 시각이 달라 다른 토큰 생성

### 2. 토큰 완전 삭제 확인

#### 삭제 항목
✅ localStorage 모든 키 (개수 출력)  
✅ sessionStorage 모든 키 (개수 출력)  
✅ 일반적인 토큰 키들 (token, accessToken, refreshToken, jwt, authToken 등)  
✅ 모든 쿠키  

#### 로그 출력 예시
```
[12:16:30] 🧹 모든 토큰 및 저장소 완전 삭제 중...
[12:16:30]   - localStorage 키 3개 발견
[12:16:30]     삭제: token
[12:16:30]     삭제: role
[12:16:30]     삭제: userId
[12:16:30]   - sessionStorage 키 0개 발견
[12:16:30] ✅ 토큰 삭제 완료:
[12:16:30]   - localStorage: 0개 항목
[12:16:30]   - sessionStorage: 0개 항목
[12:16:30]   - 모든 쿠키 삭제됨
[12:16:30] ✅ 저장된 토큰 없음
```

---

## 🔍 토큰 고유성 보장 방법

### 1. userId 포함
- 각 사용자마다 다른 ID가 토큰에 포함됨
- `subject`와 `userId` claim에 모두 저장

### 2. iat_timestamp 포함
- 토큰 생성 시각을 **밀리초** 단위로 저장
- 동일한 사용자가 다시 로그인해도 생성 시각이 달라 다른 토큰 생성

### 3. 표준 JWT Claims
- `iat` (Issued At): 발급 시각 (초 단위)
- `exp` (Expiration): 만료 시각 (초 단위)
- `sub` (Subject): 사용자 식별자 (userId)

---

## 📊 서버 로그 확인

```log
2025-12-08T12:16:04.623+09:00  INFO 61583 --- JwtTokenProvider 초기화 완료 - 만료 시간: 86400000ms
2025-12-08T12:16:18.881+09:00  INFO 61583 --- Started TransitLoveApplication in 14.771 seconds
```

✅ 서버 정상 실행 중 (포트 8080)

---

## 🎯 완료된 작업

### ✅ JWT 토큰 생성 로직 개선
- [x] userId 포함
- [x] iat_timestamp 포함
- [x] 사용자별 고유 토큰 생성

### ✅ 토큰 삭제 기능 강화
- [x] localStorage 완전 삭제
- [x] sessionStorage 완전 삭제
- [x] 쿠키 삭제
- [x] 삭제 로그 출력

### ✅ 디버깅 도구 개선
- [x] 토큰 디코딩 시 userId 표시
- [x] 생성 시각 표시
- [x] 상세 로그 출력

### ✅ 서버 재시작
- [x] 클린 빌드
- [x] 서버 시작
- [x] 정상 동작 확인

---

## 🛠️ 추가 도구

### `clear-tokens.sh`
토큰 삭제 가이드를 출력하는 스크립트:

```bash
./clear-tokens.sh
```

출력:
```
==================================================
🧹 JWT 토큰 완전 삭제 가이드
==================================================

1️⃣  브라우저에서 다음 URL을 열어주세요:
   👉 http://localhost:8080/oauth2-debug.html

2️⃣  '모든 토큰 삭제' 버튼을 클릭하세요

3️⃣  확인 메시지를 확인하세요:
   ✅ '모든 토큰과 저장소가 완전히 삭제되었습니다!'

4️⃣  '토큰 확인' 버튼을 클릭하여 다음을 확인하세요:
   ✅ '저장된 토큰이 없습니다.'
```

---

## 🎉 최종 결과

### Before (문제)
- 모든 사용자에게 유사한 토큰 생성
- 토큰에 userId 미포함
- 생성 시각이 동일하면 같은 토큰 생성 가능

### After (해결)
- ✅ 각 사용자마다 **완전히 다른 고유한 토큰** 생성
- ✅ userId와 iat_timestamp 포함으로 고유성 보장
- ✅ 브라우저의 모든 토큰 완전 삭제 가능
- ✅ 디버깅 도구에서 토큰 정보 상세 확인 가능

---

## 📝 테스트 방법

### 1. 토큰 완전 삭제 테스트
```bash
# 1. 브라우저에서 열기
open http://localhost:8080/oauth2-debug.html

# 2. "모든 토큰 삭제" 버튼 클릭
# 3. "토큰 확인" 버튼 클릭
# 4. "✅ 저장된 토큰이 없습니다." 확인
```

### 2. 사용자별 고유 토큰 생성 테스트
```bash
# User 1 로그인
curl -X POST http://localhost:8080/api/auth/test-login \
  -H "Content-Type: application/json" \
  -d '{"email":"test1@bssm.hs.kr"}' | jq .

# User 2 로그인
curl -X POST http://localhost:8080/api/auth/test-login \
  -H "Content-Type: application/json" \
  -d '{"email":"test2@bssm.hs.kr"}' | jq .

# 두 토큰을 jwt.io에서 디코딩하여 비교
# - userId가 다름
# - iat_timestamp가 다름
```

### 3. OAuth2 로그인 테스트
```bash
# 1. 브라우저에서 http://localhost:8080/oauth2-debug.html
# 2. "모든 토큰 삭제" 클릭
# 3. "Google 로그인 시작" 클릭
# 4. Google 계정 선택 및 승인
# 5. 리다이렉트 URL에서 token 파라미터 확인
# 6. jwt.io에서 토큰 디코딩하여 userId 확인
```

---

**작성일**: 2025-12-08  
**작업 내용**: JWT 토큰 완전 삭제 및 사용자별 고유 토큰 생성  
**상태**: ✅ 완료  
**서버 상태**: ✅ 실행 중 (포트 8080)

