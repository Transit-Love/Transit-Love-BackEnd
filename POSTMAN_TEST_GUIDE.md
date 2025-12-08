# Transit Love API - Postman 테스트 가이드

## 📋 사전 준비

### 1. 서버 실행
```bash
cd /Users/hanjiwon/Desktop/transitLove/BackEnd/transitLove
./start-server.sh
```

### 2. 기본 정보
- **Base URL**: `http://localhost:8080`
- **관리자 계정**: `admin@transitlove.com`
- **테스트 계정**: `test1@example.com` ~ `test6@example.com`

---

## 🔐 1단계: 관리자 로그인 (JWT 토큰 발급)

### Request
```
POST http://localhost:8080/api/auth/test-login
Content-Type: application/json
```

### Body (raw JSON)
```json
{
  "email": "admin@transitlove.com"
}
```

### Response
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "admin@transitlove.com",
  "name": "관리자",
  "role": "ADMIN"
}
```

**✅ 중요: `accessToken` 값을 복사해두세요!**

---

## 🔑 2단계: Postman 환경 변수 설정

### 방법 1: Collection Variables (권장)
1. Postman에서 Collection 생성: "Transit Love API"
2. Collection → Variables 탭
3. 변수 추가:
   - Variable: `adminToken`
   - Initial Value: `{복사한 accessToken}`
   - Current Value: `{복사한 accessToken}`

### 방법 2: Tests 탭 자동화
로그인 요청의 **Tests** 탭에 추가:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

var jsonData = pm.response.json();
pm.collectionVariables.set("adminToken", jsonData.accessToken);
```

---

## 📊 3단계: Admin API 테스트

### 3-1. 전체 매칭 조회

#### Request
```
GET http://localhost:8080/api/admin/matches
Authorization: Bearer {{adminToken}}
```

#### Headers
| Key | Value |
|-----|-------|
| Authorization | Bearer {{adminToken}} |

#### Response 예시
```json
{
  "totalMatches": 3,
  "activeMatches": 3,
  "inactiveMatches": 0,
  "matches": [
    {
      "matchId": 1,
      "profile1Id": 1,
      "profile1Nickname": "김철수",
      "profile1Mbti": "ENFP",
      "profile2Id": 2,
      "profile2Nickname": "이영희",
      "profile2Mbti": "INFP",
      "isActive": true,
      "matchType": "AUTO_MATCHED",
      "similarityScore": 0.85,
      "similarityPercentage": 85,
      "matchedAt": "2025-12-08T10:30:00",
      "unmatchedAt": null
    }
  ]
}
```

---

### 3-2. 전체 프로필 조회

#### Request
```
GET http://localhost:8080/api/admin/profiles
Authorization: Bearer {{adminToken}}
```

#### Response 예시
```json
{
  "totalProfiles": 6,
  "profiles": [
    {
      "profileId": 1,
      "nickname": "김철수",
      "mbti": "ENFP",
      "userId": 2,
      "userEmail": "test1@example.com",
      "keywordCount": 0,
      "balanceGameAnswerCount": 5,
      "createdAt": "2025-12-08T10:00:00"
    }
  ]
}
```

---

### 3-3. 매칭 통계 조회

#### Request
```
GET http://localhost:8080/api/admin/stats
Authorization: Bearer {{adminToken}}
```

#### Response 예시
```json
{
  "totalProfiles": 6,
  "totalMatches": 3,
  "activeMatches": 3,
  "inactiveMatches": 0,
  "autoMatches": 3,
  "manualMatches": 0,
  "matchedProfiles": 6,
  "unmatchedProfiles": 0,
  "averageSimilarityScore": "78.33%"
}
```

---

## 🎯 4단계: 일반 사용자 API 테스트

### 4-1. 일반 사용자 로그인

#### Request
```
POST http://localhost:8080/api/auth/test-login
Content-Type: application/json
```

#### Body
```json
{
  "email": "test1@example.com"
}
```

#### Response
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "test1@example.com",
  "name": "김철수",
  "role": "USER"
}
```

**✅ `accessToken`을 변수 `userToken`에 저장**

---

### 4-2. 자동 매칭 실행

#### Request
```
POST http://localhost:8080/api/matching/auto-match
Authorization: Bearer {{userToken}}
```

#### Response
```json
{
  "matchId": 4,
  "myProfile": {
    "id": 1,
    "nickname": "김철수",
    "mbti": "ENFP"
  },
  "partnerProfile": {
    "id": 2,
    "nickname": "이영희",
    "mbti": "INFP"
  },
  "matchType": "AUTO_MATCHED",
  "similarityPercentage": 85,
  "matchedAt": "2025-12-08T11:00:00"
}
```

---

### 4-3. 매칭 추천 조회

#### Request
```
GET http://localhost:8080/api/matching/recommendations?limit=5
Authorization: Bearer {{userToken}}
```

#### Response
```json
[
  {
    "profileId": 3,
    "nickname": "박민수",
    "mbti": "ENFJ",
    "similarityPercentage": 92,
    "mbtiScore": 75,
    "balanceGameScore": 100
  },
  {
    "profileId": 4,
    "nickname": "최지은",
    "mbti": "ISTJ",
    "similarityPercentage": 68,
    "mbtiScore": 25,
    "balanceGameScore": 80
  }
]
```

---

### 4-4. 채팅 목록 조회

#### Request
```
GET http://localhost:8080/api/chat/list
Authorization: Bearer {{userToken}}
```

#### Response
```json
{
  "matchedProfile": {
    "matchId": 1,
    "profileId": 2,
    "nickname": "이영희",
    "mbti": "INFP",
    "matchedAt": "2025-12-08T10:30:00",
    "unreadMessageCount": 0,
    "lastMessage": null,
    "lastMessageTime": null
  },
  "otherProfiles": [
    {
      "profileId": 3,
      "nickname": "박민수",
      "mbti": "ENFJ",
      "createdAt": "2025-12-08T10:00:00"
    }
  ]
}
```

---

### 4-5. 임시 채팅 생성

#### Request
```
POST http://localhost:8080/api/temp-chat?targetProfileId=3
Authorization: Bearer {{userToken}}
```

#### Response
```json
{
  "tempChatId": 1,
  "otherProfile": {
    "id": 3,
    "nickname": "박민수",
    "mbti": "ENFJ"
  },
  "createdAt": "2025-12-08T11:30:00",
  "expiresAt": "2025-12-08T13:30:00",
  "remainingMinutes": 120,
  "isExpired": false,
  "unreadCount": 0,
  "lastMessage": null,
  "lastMessageTime": null
}
```

---

## 🚫 5단계: 권한 없는 접근 테스트

### 일반 사용자가 Admin API 호출 시

#### Request
```
GET http://localhost:8080/api/admin/stats
Authorization: Bearer {{userToken}}
```

#### Response (403 Forbidden)
```json
{
  "timestamp": "2025-12-08T11:45:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/admin/stats"
}
```

---

## 📦 Postman Collection 구조

```
Transit Love API
├── 🔐 Authentication
│   ├── Admin 로그인
│   └── User 로그인
├── 👨‍💼 Admin APIs (ADMIN 권한 필요)
│   ├── 전체 매칭 조회
│   ├── 전체 프로필 조회
│   └── 통계 조회
├── 💑 Matching APIs
│   ├── 자동 매칭 (전체)
│   ├── 자동 매칭 (개인)
│   └── 매칭 추천
├── 💬 Chat APIs
│   ├── 채팅 목록 조회
│   ├── 커플 채팅 메시지 조회
│   └── 메시지 전송
└── ⏰ Temp Chat APIs
    ├── 임시 채팅 생성
    ├── 임시 채팅 목록
    └── 임시 채팅 메시지
```

---

## 🔧 트러블슈팅

### 1. "Access Denied" 오류
**원인**: JWT 토큰이 없거나 만료됨  
**해결**: 로그인 API를 다시 호출하여 새 토큰 발급

### 2. "Invalid token" 오류
**원인**: JWT 토큰 형식 오류  
**해결**: Authorization 헤더 확인
```
올바른 형식: Bearer eyJhbGciOiJIUzI1NiJ9...
잘못된 형식: eyJhbGciOiJIUzI1NiJ9... (Bearer 없음)
```

### 3. 403 Forbidden on Admin API
**원인**: USER 역할로 Admin API 호출  
**해결**: admin@transitlove.com으로 로그인한 토큰 사용

### 4. 서버 연결 실패
**원인**: 서버가 실행되지 않음  
**해결**: 
```bash
lsof -i :8080  # 8080 포트 확인
./start-server.sh  # 서버 시작
```

---

## 📝 빠른 시작 체크리스트

- [ ] 서버 실행 확인 (http://localhost:8080)
- [ ] Admin 로그인 → `adminToken` 저장
- [ ] Admin API 테스트 (매칭, 프로필, 통계)
- [ ] User 로그인 → `userToken` 저장
- [ ] 자동 매칭 실행
- [ ] 채팅 목록 조회
- [ ] 임시 채팅 생성

---

## 💡 팁

### 1. 환경 변수 활용
```
{{baseUrl}} = http://localhost:8080
{{adminToken}} = {관리자 JWT 토큰}
{{userToken}} = {사용자 JWT 토큰}
```

### 2. Pre-request Script로 자동 로그인
Collection의 Pre-request Script에 추가:
```javascript
if (!pm.collectionVariables.get("adminToken")) {
    pm.sendRequest({
        url: pm.variables.get("baseUrl") + "/api/auth/test-login",
        method: 'POST',
        header: {'Content-Type': 'application/json'},
        body: {
            mode: 'raw',
            raw: JSON.stringify({email: "admin@transitlove.com"})
        }
    }, function (err, res) {
        pm.collectionVariables.set("adminToken", res.json().accessToken);
    });
}
```

### 3. 토큰 만료 시간
- 기본: 24시간 (86400000ms)
- 만료 후 재로그인 필요

---

## 🎉 완료!

이제 Postman에서 Transit Love API를 자유롭게 테스트할 수 있습니다!

