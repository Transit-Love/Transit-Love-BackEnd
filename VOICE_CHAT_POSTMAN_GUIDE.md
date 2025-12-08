# 음성 채팅 API Postman 테스트 가이드

## 🚀 빠른 시작

### 1. JWT 토큰 발급

먼저 OAuth2 로그인을 통해 JWT 토큰을 발급받으세요.

```
GET http://localhost:8080/oauth2/authorization/google
```

로그인 후 발급받은 `accessToken`을 사용합니다.

---

## 📋 REST API 테스트

### 1. 음소거 상태 업데이트 (명세서 버전)

**Request**:
```http
POST http://localhost:8080/api/voice/mute
Content-Type: application/json
Authorization: Bearer YOUR_JWT_TOKEN

{
  "chatRoomId": 1,
  "profileId": 1,
  "isMuted": true
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Mute status updated"
}
```

---

### 2. 음소거 상태 조회

**Request**:
```http
GET http://localhost:8080/api/voice/room/1/mute/1
Authorization: Bearer YOUR_JWT_TOKEN
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "음소거 상태를 조회했습니다.",
  "data": {
    "chatRoomId": 1,
    "profileId": 1,
    "isMuted": true,
    "timestamp": "2025-12-09T10:30:00Z"
  }
}
```

---

### 3. 통화 기록 저장

**Request**:
```http
POST http://localhost:8080/api/voice/call/history
Content-Type: application/json
Authorization: Bearer YOUR_JWT_TOKEN

{
  "chatRoomId": 1,
  "callerProfileId": 1,
  "receiverProfileId": 2,
  "startTime": "2025-12-09T10:30:00Z",
  "endTime": "2025-12-09T10:45:30Z",
  "duration": 930,
  "callStatus": "COMPLETED",
  "voiceModulationUsed": false
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "message": "통화 기록이 저장되었습니다.",
  "data": {
    "callHistoryId": 1,
    "chatRoomId": 1,
    "callerProfileId": 1,
    "receiverProfileId": 2,
    "startTime": "2025-12-09T10:30:00Z",
    "endTime": "2025-12-09T10:45:30Z",
    "duration": 930,
    "callStatus": "COMPLETED",
    "voiceModulationUsed": false,
    "createdAt": "2025-12-09T10:45:31Z"
  }
}
```

**callStatus 값**:
- `COMPLETED`: 정상 완료
- `CANCELLED`: 발신자가 취소
- ❌ `REJECTED`, `MISSED`는 저장하지 않음 (명세서 요구사항)

---

### 4. 채팅방별 통화 기록 조회

**Request**:
```http
GET http://localhost:8080/api/voice/call/history?chatRoomId=1&page=0&size=20
Authorization: Bearer YOUR_JWT_TOKEN
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "통화 기록을 조회했습니다.",
  "data": {
    "content": [
      {
        "callHistoryId": 1,
        "chatRoomId": 1,
        "callerProfileId": 1,
        "receiverProfileId": 2,
        "startTime": "2025-12-09T10:30:00Z",
        "endTime": "2025-12-09T10:45:30Z",
        "duration": 930,
        "callStatus": "COMPLETED",
        "voiceModulationUsed": false
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20
    },
    "totalElements": 1,
    "totalPages": 1,
    "last": true,
    "first": true
  }
}
```

---

### 5. 내 통화 기록 조회

**Request**:
```http
GET http://localhost:8080/api/voice/call/history/my?page=0&size=20
Authorization: Bearer YOUR_JWT_TOKEN
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "통화 기록을 조회했습니다.",
  "data": {
    "content": [
      {
        "callHistoryId": 1,
        "chatRoomId": 1,
        "callerProfileId": 1,
        "receiverProfileId": 2,
        "startTime": "2025-12-09T10:30:00Z",
        "endTime": "2025-12-09T10:45:30Z",
        "duration": 930,
        "callStatus": "COMPLETED",
        "voiceModulationUsed": false
      },
      {
        "callHistoryId": 2,
        "chatRoomId": 3,
        "callerProfileId": 5,
        "receiverProfileId": 1,
        "startTime": "2025-12-09T11:00:00Z",
        "endTime": "2025-12-09T11:15:00Z",
        "duration": 900,
        "callStatus": "COMPLETED",
        "voiceModulationUsed": true
      }
    ],
    "totalElements": 2,
    "totalPages": 1
  }
}
```

**설명**: 내가 발신자이거나 수신자인 모든 통화 기록

---

### 6. 통화 통계 조회

**Request**:
```http
GET http://localhost:8080/api/voice/call/statistics?chatRoomId=1
Authorization: Bearer YOUR_JWT_TOKEN
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "통화 통계를 조회했습니다.",
  "data": {
    "chatRoomId": 1,
    "totalCalls": 15,
    "completedCalls": 15,
    "totalDuration": 12450,
    "averageDuration": 830,
    "totalMinutes": 207,
    "formattedTotalDuration": "3시간 27분",
    "lastCallTime": "2025-12-09T10:45:30Z",
    "voiceModulationUsedCount": 8
  }
}
```

**필드 설명**:
- `totalCalls`: 전체 통화 횟수
- `completedCalls`: 완료된 통화 횟수
- `totalDuration`: 전체 통화 시간 (초)
- `averageDuration`: 평균 통화 시간 (초)
- `totalMinutes`: 전체 통화 시간 (분)
- `formattedTotalDuration`: 사용자 친화적 형식
- `lastCallTime`: 마지막 통화 종료 시각
- `voiceModulationUsedCount`: 음성 변조 사용 횟수

---

## 🧪 WebSocket 시그널링 테스트

WebSocket은 Postman보다 **브라우저 테스트 페이지**를 사용하는 것을 권장합니다.

### 테스트 페이지 접속

```
http://localhost:8080/voice-signaling-test.html
```

### 테스트 절차

1. **JWT 토큰 입력**
2. **채팅방 정보 입력**
   - 채팅방 ID
   - 내 프로필 ID
   - 상대방 프로필 ID
3. **"연결" 버튼 클릭**
4. **시그널 타입 선택**
5. **"시그널 전송" 버튼 클릭**
6. **수신 로그 확인**

### 지원 시그널 타입

- `VOICE_CALL_REQUEST` - 통화 요청
- `VOICE_CALL_ACCEPT` - 통화 수락
- `VOICE_CALL_REJECT` - 통화 거절
- `VOICE_CALL_END` - 통화 종료
- `VOICE_CALL_CANCEL` - 통화 취소
- `OFFER` - WebRTC Offer (SDP 필요)
- `ANSWER` - WebRTC Answer (SDP 필요)
- `ICE_CANDIDATE` - ICE Candidate (candidate 정보 필요)
- `MUTE_STATUS_CHANGED` - 음소거 상태 변경
- `SPEAKER_STATUS_CHANGED` - 스피커 상태 변경

---

## 🎯 전체 시나리오 테스트

### 시나리오 1: 정상 통화 흐름

1. **통화 요청** (WebSocket)
   ```json
   {
     "type": "VOICE_CALL_REQUEST",
     "chatRoomId": 1,
     "fromProfileId": 1,
     "toProfileId": 2
   }
   ```

2. **통화 수락** (WebSocket)
   ```json
   {
     "type": "VOICE_CALL_ACCEPT",
     "chatRoomId": 1,
     "fromProfileId": 2,
     "toProfileId": 1
   }
   ```

3. **WebRTC Offer** (WebSocket)
   ```json
   {
     "type": "OFFER",
     "chatRoomId": 1,
     "fromProfileId": 1,
     "toProfileId": 2,
     "sdp": "v=0\r\no=..."
   }
   ```

4. **WebRTC Answer** (WebSocket)
   ```json
   {
     "type": "ANSWER",
     "chatRoomId": 1,
     "fromProfileId": 2,
     "toProfileId": 1,
     "sdp": "v=0\r\no=..."
   }
   ```

5. **ICE Candidate 교환** (WebSocket, 여러 번)
   ```json
   {
     "type": "ICE_CANDIDATE",
     "chatRoomId": 1,
     "fromProfileId": 1,
     "toProfileId": 2,
     "iceCandidate": {
       "candidate": "candidate:...",
       "sdpMid": "0",
       "sdpMLineIndex": 0
     }
   }
   ```

6. **음소거 상태 변경** (WebSocket 또는 REST)
   ```http
   POST /api/voice/mute
   {
     "chatRoomId": 1,
     "profileId": 1,
     "isMuted": true
   }
   ```

7. **통화 종료** (WebSocket)
   ```json
   {
     "type": "VOICE_CALL_END",
     "chatRoomId": 1,
     "fromProfileId": 1,
     "toProfileId": 2
   }
   ```

8. **통화 기록 저장** (REST)
   ```http
   POST /api/voice/call/history
   {
     "chatRoomId": 1,
     "callerProfileId": 1,
     "receiverProfileId": 2,
     "startTime": "2025-12-09T10:30:00Z",
     "endTime": "2025-12-09T10:45:30Z",
     "duration": 930,
     "callStatus": "COMPLETED",
     "voiceModulationUsed": false
   }
   ```

9. **통화 통계 조회** (REST)
   ```http
   GET /api/voice/call/statistics?chatRoomId=1
   ```

---

### 시나리오 2: 통화 거절

1. **통화 요청** (WebSocket)
   ```json
   {
     "type": "VOICE_CALL_REQUEST",
     "chatRoomId": 1,
     "fromProfileId": 1,
     "toProfileId": 2
   }
   ```

2. **통화 거절** (WebSocket)
   ```json
   {
     "type": "VOICE_CALL_REJECT",
     "chatRoomId": 1,
     "fromProfileId": 2,
     "toProfileId": 1
   }
   ```

3. **통화 기록 저장하지 않음** ✅
   - 명세서: REJECTED 상태는 저장하지 않음

---

### 시나리오 3: 통화 취소

1. **통화 요청** (WebSocket)
   ```json
   {
     "type": "VOICE_CALL_REQUEST",
     "chatRoomId": 1,
     "fromProfileId": 1,
     "toProfileId": 2
   }
   ```

2. **통화 취소** (WebSocket)
   ```json
   {
     "type": "VOICE_CALL_CANCEL",
     "chatRoomId": 1,
     "fromProfileId": 1,
     "toProfileId": 2
   }
   ```

3. **통화 기록 저장 (선택)**
   - CANCELLED 상태로 저장 가능

---

## ⚠️ 에러 케이스 테스트

### 1. 인증 실패

**Request**:
```http
POST http://localhost:8080/api/voice/call/history
Authorization: Bearer INVALID_TOKEN
```

**Response** (401 Unauthorized):
```json
{
  "timestamp": "2025-12-09T10:30:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid authentication token",
  "path": "/api/voice/call/history"
}
```

---

### 2. 잘못된 요청 데이터

**Request**:
```http
POST http://localhost:8080/api/voice/call/history
{
  "chatRoomId": 1,
  "callerProfileId": 1,
  "startTime": "2025-12-09T10:30:00Z",
  "endTime": "2025-12-09T10:45:30Z"
}
```

**Response** (400 Bad Request):
```json
{
  "success": false,
  "message": "receiverProfileId는 필수입니다.",
  "errorCode": "INVALID_REQUEST"
}
```

---

### 3. 채팅방 접근 권한 없음

**Request**:
```http
GET http://localhost:8080/api/voice/call/history?chatRoomId=999
Authorization: Bearer YOUR_JWT_TOKEN
```

**Response** (403 Forbidden):
```json
{
  "timestamp": "2025-12-09T10:30:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "해당 채팅방에 접근 권한이 없습니다.",
  "path": "/api/voice/call/history"
}
```

---

## 📊 Postman Collection

### 환경 변수 설정

```json
{
  "BASE_URL": "http://localhost:8080",
  "JWT_TOKEN": "YOUR_JWT_TOKEN_HERE",
  "CHAT_ROOM_ID": "1",
  "MY_PROFILE_ID": "1",
  "PARTNER_PROFILE_ID": "2"
}
```

### Collection 구성

1. **음성 채팅 API**
   - 1.1 음소거 상태 업데이트
   - 1.2 음소거 상태 조회
   - 1.3 통화 기록 저장
   - 1.4 채팅방별 통화 기록 조회
   - 1.5 내 통화 기록 조회
   - 1.6 통화 통계 조회

2. **에러 케이스**
   - 2.1 인증 실패
   - 2.2 잘못된 요청
   - 2.3 권한 없음

---

## ✅ 체크리스트

### REST API 테스트
- [ ] 음소거 상태 업데이트 (POST /api/voice/mute)
- [ ] 음소거 상태 조회 (GET /api/voice/room/{id}/mute/{profileId})
- [ ] 통화 기록 저장 (POST /api/voice/call/history)
- [ ] 채팅방별 통화 기록 조회 (GET /api/voice/call/history)
- [ ] 내 통화 기록 조회 (GET /api/voice/call/history/my)
- [ ] 통화 통계 조회 (GET /api/voice/call/statistics)

### WebSocket 시그널링 테스트
- [ ] WebSocket 연결
- [ ] 토픽 구독 확인
- [ ] VOICE_CALL_REQUEST 전송/수신
- [ ] VOICE_CALL_ACCEPT 전송/수신
- [ ] OFFER 전송/수신 (SDP 포함)
- [ ] ANSWER 전송/수신 (SDP 포함)
- [ ] ICE_CANDIDATE 전송/수신
- [ ] senderId 필터링 동작 확인
- [ ] 통화 종료 (VOICE_CALL_END)

### 에러 처리 테스트
- [ ] 인증 실패 (401)
- [ ] 잘못된 요청 (400)
- [ ] 권한 없음 (403)
- [ ] 리소스 없음 (404)

---

**작성일**: 2025-12-09  
**버전**: 1.0.0  
**테스트 도구**: Postman, 브라우저

