# Admin Chat API 문서

## 개요
어드민 전용 채팅 API입니다. 모든 커플의 매칭 정보와 채팅 내역을 조회할 수 있습니다.

**주의사항:**
- 어드민만 접근 가능합니다 (ROLE_ADMIN 필요)
- 조회만 가능하며, 메시지 전송은 불가능합니다
- 모든 요청에 JWT 토큰이 필요합니다

---

## 📋 API 목록

### 1. 모든 커플 매칭 목록 조회

**Endpoint:** `GET /api/admin/chat/matches`

**설명:** 모든 커플의 매칭 정보를 조회합니다. 각 매칭의 정보, 메시지 수, 최근 메시지를 포함합니다.

**권한:** ADMIN

**Query Parameters:**
- `includeInactive` (optional, boolean, default: false): 비활성 매칭 포함 여부

**Request Example:**
```bash
# 활성 매칭만 조회
curl -X GET "http://localhost:8080/api/admin/chat/matches" \
  -H "Authorization: Bearer {ADMIN_JWT_TOKEN}"

# 모든 매칭 조회 (비활성 포함)
curl -X GET "http://localhost:8080/api/admin/chat/matches?includeInactive=true" \
  -H "Authorization: Bearer {ADMIN_JWT_TOKEN}"
```

**Response (200 OK):**
```json
{
  "matches": [
    {
      "matchId": 1,
      "profile1": {
        "profileId": 1,
        "nickname": "김철수",
        "mbti": "ENFP"
      },
      "profile2": {
        "profileId": 2,
        "nickname": "이영희",
        "mbti": "INFJ"
      },
      "isActive": true,
      "matchType": "AUTO",
      "similarityScore": 85.5,
      "matchedAt": "2024-12-07T10:30:00",
      "unmatchedAt": null,
      "totalMessageCount": 45,
      "lastMessage": "오늘 저녁 뭐 먹을래?",
      "lastMessageTime": "2024-12-08T14:20:00"
    },
    {
      "matchId": 2,
      "profile1": {
        "profileId": 3,
        "nickname": "박민수",
        "mbti": "ISTJ"
      },
      "profile2": {
        "profileId": 4,
        "nickname": "최지은",
        "mbti": "ESFP"
      },
      "isActive": true,
      "matchType": "MANUAL",
      "similarityScore": 0.0,
      "matchedAt": "2024-12-06T15:45:00",
      "unmatchedAt": null,
      "totalMessageCount": 12,
      "lastMessage": "안녕하세요!",
      "lastMessageTime": "2024-12-06T16:00:00"
    }
  ],
  "totalCount": 2
}
```

**Response Fields:**
- `matches`: 매칭 목록
  - `matchId`: 매칭 ID
  - `profile1`: 첫 번째 프로필 정보
    - `profileId`: 프로필 ID
    - `nickname`: 닉네임
    - `mbti`: MBTI
  - `profile2`: 두 번째 프로필 정보
  - `isActive`: 매칭 활성 여부
  - `matchType`: 매칭 타입 (AUTO, MANUAL)
  - `similarityScore`: 유사도 점수
  - `matchedAt`: 매칭 시작 시간
  - `unmatchedAt`: 매칭 해제 시간 (null이면 활성)
  - `totalMessageCount`: 총 메시지 수
  - `lastMessage`: 최근 메시지 내용
  - `lastMessageTime`: 최근 메시지 시간
- `totalCount`: 총 매칭 수

**Error Responses:**

- **401 Unauthorized:** JWT 토큰이 없거나 유효하지 않음
```json
{
  "error": "Unauthorized",
  "message": "인증이 필요합니다"
}
```

- **403 Forbidden:** 어드민 권한이 없음
```json
{
  "error": "Forbidden",
  "message": "접근 권한이 없습니다"
}
```

---

### 2. 특정 커플의 채팅 메시지 조회

**Endpoint:** `GET /api/admin/chat/matches/{matchId}/messages`

**설명:** 특정 매칭의 모든 채팅 메시지를 시간순으로 조회합니다.

**권한:** ADMIN

**Path Parameters:**
- `matchId` (required, Long): 매칭 ID

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/admin/chat/matches/1/messages" \
  -H "Authorization: Bearer {ADMIN_JWT_TOKEN}"
```

**Response (200 OK):**
```json
{
  "matchId": 1,
  "matchProfiles": {
    "profile1": {
      "profileId": 1,
      "nickname": "김철수",
      "mbti": "ENFP"
    },
    "profile2": {
      "profileId": 2,
      "nickname": "이영희",
      "mbti": "INFJ"
    },
    "matchedAt": "2024-12-07T10:30:00",
    "isActive": true
  },
  "messages": [
    {
      "messageId": 1,
      "senderProfileId": 1,
      "senderNickname": "김철수",
      "content": "안녕하세요!",
      "type": "TEXT",
      "isRead": true,
      "sentAt": "2024-12-07T10:35:00",
      "readAt": "2024-12-07T10:36:00"
    },
    {
      "messageId": 2,
      "senderProfileId": 2,
      "senderNickname": "이영희",
      "content": "안녕하세요~ 반가워요!",
      "type": "TEXT",
      "isRead": true,
      "sentAt": "2024-12-07T10:36:00",
      "readAt": "2024-12-07T10:37:00"
    },
    {
      "messageId": 3,
      "senderProfileId": 1,
      "senderNickname": "김철수",
      "content": "오늘 저녁 뭐 먹을래?",
      "type": "TEXT",
      "isRead": false,
      "sentAt": "2024-12-08T14:20:00",
      "readAt": null
    }
  ],
  "totalMessageCount": 3
}
```

**Response Fields:**
- `matchId`: 매칭 ID
- `matchProfiles`: 매칭된 프로필 정보
  - `profile1`: 첫 번째 프로필
  - `profile2`: 두 번째 프로필
  - `matchedAt`: 매칭 시작 시간
  - `isActive`: 활성 여부
- `messages`: 메시지 목록 (시간순 정렬)
  - `messageId`: 메시지 ID
  - `senderProfileId`: 발신자 프로필 ID
  - `senderNickname`: 발신자 닉네임
  - `content`: 메시지 내용
  - `type`: 메시지 타입 (TEXT, IMAGE, SYSTEM)
  - `isRead`: 읽음 여부
  - `sentAt`: 전송 시간
  - `readAt`: 읽은 시간 (읽지 않았으면 null)
- `totalMessageCount`: 총 메시지 수

**Error Responses:**

- **401 Unauthorized:** JWT 토큰이 없거나 유효하지 않음
```json
{
  "error": "Unauthorized",
  "message": "인증이 필요합니다"
}
```

- **403 Forbidden:** 어드민 권한이 없음
```json
{
  "error": "Forbidden",
  "message": "접근 권한이 없습니다"
}
```

- **404 Not Found:** 매칭을 찾을 수 없음
```json
{
  "error": "Not Found",
  "message": "매칭을 찾을 수 없습니다. ID: 999"
}
```

---

## 🔐 인증 방법

모든 어드민 API는 JWT 토큰을 사용한 인증이 필요합니다.

### JWT 토큰 헤더 형식
```
Authorization: Bearer {JWT_TOKEN}
```

### 어드민 권한 확인
- JWT 토큰에 `ROLE_ADMIN` 권한이 포함되어야 합니다
- 일반 사용자는 어드민 API에 접근할 수 없습니다

---

## 📊 사용 시나리오

### 1. 전체 매칭 현황 모니터링
```bash
# 1단계: 모든 활성 매칭 조회
curl -X GET "http://localhost:8080/api/admin/chat/matches" \
  -H "Authorization: Bearer {ADMIN_JWT_TOKEN}"

# 2단계: 비활성 매칭 포함 전체 조회
curl -X GET "http://localhost:8080/api/admin/chat/matches?includeInactive=true" \
  -H "Authorization: Bearer {ADMIN_JWT_TOKEN}"
```

### 2. 특정 커플의 채팅 모니터링
```bash
# 1단계: 매칭 목록에서 관심 있는 matchId 확인
curl -X GET "http://localhost:8080/api/admin/chat/matches" \
  -H "Authorization: Bearer {ADMIN_JWT_TOKEN}"

# 2단계: 해당 매칭의 전체 채팅 내역 조회
curl -X GET "http://localhost:8080/api/admin/chat/matches/1/messages" \
  -H "Authorization: Bearer {ADMIN_JWT_TOKEN}"
```

### 3. 문제 상황 파악
```bash
# 신고가 들어온 매칭 조회
curl -X GET "http://localhost:8080/api/admin/chat/matches/123/messages" \
  -H "Authorization: Bearer {ADMIN_JWT_TOKEN}"
```

---

## 🚀 JavaScript/TypeScript 예제

### Axios 사용 예제
```javascript
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080';
const adminToken = 'YOUR_ADMIN_JWT_TOKEN';

// Axios 인스턴스 생성
const adminApi = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Authorization': `Bearer ${adminToken}`
  }
});

// 1. 모든 매칭 조회
async function getAllMatches(includeInactive = false) {
  try {
    const response = await adminApi.get('/api/admin/chat/matches', {
      params: { includeInactive }
    });
    console.log('매칭 목록:', response.data);
    return response.data;
  } catch (error) {
    console.error('매칭 조회 실패:', error.response?.data || error.message);
    throw error;
  }
}

// 2. 특정 매칭의 메시지 조회
async function getMatchMessages(matchId) {
  try {
    const response = await adminApi.get(`/api/admin/chat/matches/${matchId}/messages`);
    console.log('메시지 목록:', response.data);
    return response.data;
  } catch (error) {
    console.error('메시지 조회 실패:', error.response?.data || error.message);
    throw error;
  }
}

// 사용 예제
async function monitorChatActivity() {
  // 모든 활성 매칭 조회
  const matchesData = await getAllMatches(false);
  
  // 각 매칭의 메시지 수 확인
  for (const match of matchesData.matches) {
    console.log(`매칭 ${match.matchId}:`, {
      커플: `${match.profile1.nickname} & ${match.profile2.nickname}`,
      메시지수: match.totalMessageCount,
      최근메시지: match.lastMessage
    });
    
    // 특정 조건에 따라 상세 조회
    if (match.totalMessageCount > 100) {
      const messages = await getMatchMessages(match.matchId);
      console.log('활발한 대화:', messages);
    }
  }
}

// 실행
monitorChatActivity();
```

### Fetch API 사용 예제
```javascript
const API_BASE_URL = 'http://localhost:8080';
const adminToken = 'YOUR_ADMIN_JWT_TOKEN';

// 1. 모든 매칭 조회
async function getAllMatches(includeInactive = false) {
  const url = `${API_BASE_URL}/api/admin/chat/matches?includeInactive=${includeInactive}`;
  
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${adminToken}`,
      'Content-Type': 'application/json'
    }
  });
  
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  
  return await response.json();
}

// 2. 특정 매칭의 메시지 조회
async function getMatchMessages(matchId) {
  const url = `${API_BASE_URL}/api/admin/chat/matches/${matchId}/messages`;
  
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${adminToken}`,
      'Content-Type': 'application/json'
    }
  });
  
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  
  return await response.json();
}
```

---

## ⚠️ 주의사항

1. **권한 관리**
   - 어드민 API는 민감한 정보를 포함하므로 반드시 ROLE_ADMIN 권한이 필요합니다
   - JWT 토큰을 안전하게 관리하세요

2. **데이터 보안**
   - 조회된 채팅 내용은 개인정보이므로 적절히 보호해야 합니다
   - 로그에 민감한 정보가 기록되지 않도록 주의하세요

3. **성능 고려**
   - 메시지가 많은 경우 응답 시간이 길어질 수 있습니다
   - 필요시 페이지네이션 기능을 추가할 수 있습니다

4. **조회 전용**
   - 어드민은 메시지를 읽기만 가능하며, 전송은 불가능합니다
   - 메시지 삭제나 수정 기능은 제공되지 않습니다

---

## 📈 통계 정보 활용

매칭 목록 API에서 제공되는 정보로 다양한 통계를 산출할 수 있습니다:

1. **활성 매칭 수:** `totalCount` 필드
2. **평균 메시지 수:** `totalMessageCount`의 평균
3. **활발한 커플 Top 10:** `totalMessageCount` 기준 정렬
4. **최근 활동 커플:** `lastMessageTime` 기준 정렬
5. **매칭 타입별 통계:** `matchType` 기준 그룹화

---

## 🔧 문제 해결

### 403 Forbidden 에러
- JWT 토큰에 ROLE_ADMIN 권한이 있는지 확인
- SecurityConfig에서 `/api/admin/**` 경로 설정 확인

### 401 Unauthorized 에러
- JWT 토큰이 유효한지 확인
- Authorization 헤더 형식이 올바른지 확인

### 404 Not Found 에러
- matchId가 존재하는지 확인
- 데이터베이스에 해당 매칭이 있는지 확인

---

## 📚 관련 문서

- [CHAT_API_README.md](./CHAT_API_README.md) - 일반 사용자 채팅 API
- [CHAT_LIST_API_README.md](./CHAT_LIST_API_README.md) - 채팅 목록 API
- [CHAT_IMPLEMENTATION_SUMMARY.md](./CHAT_IMPLEMENTATION_SUMMARY.md) - 채팅 기능 구현 요약

