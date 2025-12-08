# 마지막 선택(Final Choice) API - Postman 테스트 가이드

## 📋 개요

마지막 선택 기능을 Postman에서 테스트하는 방법을 설명합니다.

## 🔧 사전 준비

1. **서버 실행**: `./start-server.sh`
2. **JWT 토큰 획득**: OAuth2 로그인 또는 테스트 토큰 발급
3. **프로필 생성**: 최소 3개 이상의 프로필 필요
4. **매칭 생성**: 자동 매칭 또는 수동 매칭
5. **채팅 이력 생성**: UNMATCHED 채팅방에서 메시지 주고받기

## 📝 테스트 시나리오

### 시나리오 준비

```
프로필 A (본인)
├─ 현재 매칭: 프로필 B
└─ 채팅 이력: 프로필 C, 프로필 D (UNMATCHED)
```

## 🚀 API 테스트

### 1. 마지막 선택 옵션 조회

#### Request

```http
GET http://localhost:8080/api/chat/final-choice/options
Authorization: Bearer {YOUR_JWT_TOKEN}
Content-Type: application/json
```

#### Response 예시

```json
{
  "currentMatch": {
    "matchId": 1,
    "profileId": 2,
    "nickname": "프로필B",
    "mbti": "ENFP",
    "matchedAt": "2024-01-01T12:00:00",
    "totalMessageCount": 50
  },
  "chatPartners": [
    {
      "profileId": 3,
      "nickname": "프로필C",
      "mbti": "INFJ",
      "totalMessageCount": 20,
      "lastMessageAt": "2024-01-02T10:00:00"
    },
    {
      "profileId": 4,
      "nickname": "프로필D",
      "mbti": "INTJ",
      "totalMessageCount": 15,
      "lastMessageAt": "2024-01-02T09:30:00"
    }
  ]
}
```

#### 해석

- **currentMatch**: 현재 매칭된 프로필B
- **chatPartners**: 채팅했던 프로필C, D (메시지가 있는 경우만)

---

### 2. 마지막 선택 실행 - 케이스 1: 현재 매칭 유지

#### Request

```http
POST http://localhost:8080/api/chat/final-choice
Authorization: Bearer {YOUR_JWT_TOKEN}
Content-Type: application/json

{
  "selectedProfileId": 2
}
```

#### Response

```json
{
  "matchId": 1,
  "myProfileId": 1,
  "partnerProfileId": 2,
  "partnerNickname": "프로필B",
  "partnerMbti": "ENFP",
  "isChanged": false,
  "finalizedAt": "2024-01-03T15:30:00"
}
```

#### 결과

- `isChanged: false` → 매칭 유지
- 기존 매칭 ID 그대로 유지

---

### 3. 마지막 선택 실행 - 케이스 2: 다른 사람 선택 (매칭 변경)

#### Request

```http
POST http://localhost:8080/api/chat/final-choice
Authorization: Bearer {YOUR_JWT_TOKEN}
Content-Type: application/json

{
  "selectedProfileId": 3
}
```

#### Response

```json
{
  "matchId": 5,
  "myProfileId": 1,
  "partnerProfileId": 3,
  "partnerNickname": "프로필C",
  "partnerMbti": "INFJ",
  "isChanged": true,
  "finalizedAt": "2024-01-03T15:35:00"
}
```

#### 결과

- `isChanged: true` → 매칭 변경됨
- 새로운 매칭 ID 생성 (5)
- 프로필B와의 기존 매칭(ID:1) → `isActive: false`
- 프로필C와의 채팅방 → `UNMATCHED` → `MATCHED`

---

### 4. 변경 후 채팅 목록 확인

#### Request

```http
GET http://localhost:8080/api/chat/list
Authorization: Bearer {YOUR_JWT_TOKEN}
Content-Type: application/json
```

#### Response

```json
{
  "matchedProfile": {
    "matchId": 5,
    "profileId": 3,
    "nickname": "프로필C",
    "mbti": "INFJ",
    "matchedAt": "2024-01-03T15:35:00",
    "unreadMessageCount": 0,
    "lastMessage": "안녕하세요!",
    "lastMessageTime": "2024-01-02T10:00:00"
  },
  "otherProfiles": [
    {
      "profileId": 2,
      "nickname": "프로필B",
      "mbti": "ENFP",
      "createdAt": "2024-01-01T10:00:00"
    },
    {
      "profileId": 4,
      "nickname": "프로필D",
      "mbti": "INTJ",
      "createdAt": "2024-01-01T11:00:00"
    }
  ]
}
```

#### 해석

- **matchedProfile**: 프로필C (새로운 매칭)
- **otherProfiles**: 프로필B는 이제 일반 프로필 목록에 표시

---

## ❌ 에러 케이스

### 1. 채팅 이력이 없는 프로필 선택

```json
{
  "selectedProfileId": 99
}
```

**Response (400 Bad Request)**

```json
{
  "error": "선택한 프로필과 채팅 이력이 없습니다."
}
```

---

### 2. 메시지를 주고받지 않은 프로필 선택

```json
{
  "selectedProfileId": 5
}
```

**Response (400 Bad Request)**

```json
{
  "error": "선택한 프로필과 메시지를 주고받은 적이 없습니다."
}
```

---

### 3. 활성 매칭이 없는 경우

**Response (400 Bad Request)**

```json
{
  "error": "현재 활성 매칭이 없습니다."
}
```

---

## 🔍 데이터베이스 변경 확인

### 매칭 변경 전

**matches 테이블**

| id | profile1_id | profile2_id | is_active | match_type |
|----|-------------|-------------|-----------|------------|
| 1  | 1           | 2           | true      | AUTO       |

**chat_rooms 테이블**

| id | profile1_id | profile2_id | room_type  | match_id |
|----|-------------|-------------|------------|----------|
| 1  | 1           | 2           | MATCHED    | 1        |
| 2  | 1           | 3           | UNMATCHED  | null     |

### 매칭 변경 후 (프로필 3 선택)

**matches 테이블**

| id | profile1_id | profile2_id | is_active | match_type | unmatched_at        |
|----|-------------|-------------|-----------|------------|---------------------|
| 1  | 1           | 2           | false     | AUTO       | 2024-01-03 15:35:00 |
| 5  | 1           | 3           | true      | MANUAL     | null                |

**chat_rooms 테이블**

| id | profile1_id | profile2_id | room_type  | match_id |
|----|-------------|-------------|------------|----------|
| 1  | 1           | 2           | MATCHED    | 1        |
| 2  | 1           | 3           | MATCHED    | 5        |

**변경 사항**

1. 매칭 1: `isActive: true → false`, `unmatchedAt` 설정
2. 매칭 5: 새로 생성 (`matchType: MANUAL`)
3. 채팅방 2: `roomType: UNMATCHED → MATCHED`, `matchId: null → 5`

---

## 📊 Postman Collection

### Collection 구조

```
TransitLove API
└── Final Choice
    ├── 1. Get Final Choice Options
    ├── 2. Make Final Choice (Keep Current)
    ├── 3. Make Final Choice (Change Match)
    └── 4. Get Chat List After Choice
```

### 환경 변수

```json
{
  "baseUrl": "http://localhost:8080",
  "token": "YOUR_JWT_TOKEN",
  "myProfileId": "1",
  "currentMatchProfileId": "2",
  "newMatchProfileId": "3"
}
```

---

## 🎯 테스트 체크리스트

- [ ] 마지막 선택 옵션 조회 성공
- [ ] 현재 매칭이 올바르게 표시됨
- [ ] 채팅 파트너 목록이 올바르게 표시됨
- [ ] 현재 매칭 상대 선택 시 유지됨 (`isChanged: false`)
- [ ] 다른 사람 선택 시 매칭 변경됨 (`isChanged: true`)
- [ ] 새로운 매칭 ID가 생성됨
- [ ] 기존 매칭이 비활성화됨
- [ ] 채팅방 타입이 MATCHED로 변경됨
- [ ] 채팅 목록에 새로운 매칭이 반영됨
- [ ] 에러 케이스가 올바르게 처리됨

---

## 🛠️ 디버깅 팁

### 서버 로그 확인

```bash
tail -f server.log | grep "마지막 선택"
```

### 예상 로그

```
[INFO] 마지막 선택 옵션 조회 - 프로필: 1
[INFO] 마지막 선택 옵션 조회 완료 - 현재 매칭: 1, 채팅 파트너: 2
[INFO] 마지막 선택 실행 - 프로필: 1, 선택: 3
[INFO] 매칭 변경 시작 - 기존: 2, 새로운: 3
[INFO] 기존 매칭 해제 완료 - ID: 1
[INFO] 새로운 매칭 생성 완료 - ID: 5
[INFO] 채팅방 타입 변경 (UNMATCHED -> MATCHED) - 채팅방 ID: 2
[INFO] 마지막 선택 완료 - 프로필: 1, 변경 여부: true
```

---

## 📝 추가 테스트

### 1. 양방향 매칭 해제 확인

프로필C도 기존에 다른 사람(프로필E)과 매칭되어 있었다면, 그 매칭도 자동으로 해제됩니다.

### 2. 만료 시간 제거 확인

UNMATCHED 채팅방의 `expiresAt`이 `null`로 변경되는지 확인합니다.

```sql
SELECT id, profile1_id, profile2_id, room_type, expires_at, match_id 
FROM chat_rooms 
WHERE id = 2;
```

**변경 전**: `expires_at: 2024-01-02 12:00:00`  
**변경 후**: `expires_at: null`

---

## 🎉 테스트 완료

모든 테스트가 통과하면 마지막 선택 기능이 정상적으로 작동하는 것입니다!

