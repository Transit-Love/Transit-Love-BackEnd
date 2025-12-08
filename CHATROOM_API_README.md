# 채팅방 기반 채팅 API 가이드

## 개요
매칭 여부와 관계없이 모든 사용자 간 채팅을 지원하는 채팅방 기반 API입니다.

## 주요 기능
1. **채팅방 생성/조회**: 매칭되지 않은 사용자와도 채팅방 생성 가능
2. **채팅방 리스트**: 매칭된 채팅방과 일반 채팅방 구분하여 조회
3. **메시지 전송/조회**: 채팅방 기반으로 메시지 송수신
4. **어드민 기능**: 모든 매칭된 채팅방 조회 및 메시지 열람

---

## 사용자 API

### 1. 채팅방 생성 또는 조회
상대방과의 채팅방을 생성하거나 기존 채팅방을 조회합니다.

**Endpoint**: `POST /api/chatrooms`

**Request Body**:
```json
{
  "targetProfileId": 2
}
```

**Response** (200 OK):
```json
{
  "chatRoomId": 1,
  "matchId": null,
  "roomType": "UNMATCHED",
  "otherProfileId": 2,
  "otherNickname": "사용자2",
  "otherMbti": "INFP",
  "createdAt": "2024-01-15T10:00:00",
  "lastMessageAt": null,
  "isActive": true
}
```

**설명**:
- 이미 채팅방이 존재하면 기존 채팅방 반환
- 없으면 새 채팅방 생성
- 매칭된 사용자면 `roomType`이 "MATCHED", `matchId`가 설정됨
- 매칭되지 않은 사용자면 `roomType`이 "UNMATCHED"

---

### 2. 내 채팅방 리스트 조회
내가 참여한 모든 채팅방을 조회합니다.

**Endpoint**: `GET /api/chatrooms`

**Response** (200 OK):
```json
{
  "matchedRooms": [
    {
      "chatRoomId": 1,
      "matchId": 10,
      "roomType": "MATCHED",
      "otherProfileId": 2,
      "otherNickname": "사용자2",
      "otherMbti": "INFP",
      "createdAt": "2024-01-15T10:00:00",
      "lastMessageAt": "2024-01-15T14:30:00",
      "lastMessage": "안녕하세요!",
      "unreadCount": 3,
      "isActive": true
    }
  ],
  "unmatchedRooms": [
    {
      "chatRoomId": 2,
      "matchId": null,
      "roomType": "UNMATCHED",
      "otherProfileId": 3,
      "otherNickname": "사용자3",
      "otherMbti": "ENFJ",
      "createdAt": "2024-01-14T09:00:00",
      "lastMessageAt": "2024-01-14T15:20:00",
      "lastMessage": "좋은 하루 되세요~",
      "unreadCount": 0,
      "isActive": true
    }
  ],
  "totalCount": 2
}
```

**설명**:
- `matchedRooms`: 매칭된 사용자와의 채팅방
- `unmatchedRooms`: 매칭되지 않은 사용자와의 채팅방
- 마지막 메시지 시간 순으로 정렬

---

### 3. 채팅방 메시지 조회
특정 채팅방의 모든 메시지를 조회합니다.

**Endpoint**: `GET /api/chatrooms/{chatRoomId}/messages`

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "matchId": null,
    "senderProfileId": 1,
    "senderNickname": "나",
    "content": "안녕하세요",
    "type": "TEXT",
    "sentAt": "2024-01-15T10:00:00",
    "isRead": true,
    "readAt": "2024-01-15T10:05:00"
  },
  {
    "id": 2,
    "matchId": null,
    "senderProfileId": 2,
    "senderNickname": "상대방",
    "content": "네 안녕하세요!",
    "type": "TEXT",
    "sentAt": "2024-01-15T10:01:00",
    "isRead": false,
    "readAt": null
  }
]
```

**에러 응답**:
- 404: 채팅방을 찾을 수 없음
- 403: 채팅방 접근 권한 없음

---

### 4. 채팅방 메시지 읽음 처리
채팅방의 모든 안 읽은 메시지를 읽음 처리합니다.

**Endpoint**: `PUT /api/chatrooms/{chatRoomId}/read`

**Response** (200 OK):
```json
"메시지가 읽음 처리되었습니다."
```

---

### 5. 채팅방 비활성화
채팅방을 비활성화합니다.

**Endpoint**: `DELETE /api/chatrooms/{chatRoomId}`

**Response** (200 OK):
```json
"채팅방이 비활성화되었습니다."
```

---

## WebSocket API

### 6. 채팅방 메시지 전송 (WebSocket)
실시간으로 메시지를 전송합니다.

**Destination**: `/app/chatroom.send`

**Payload**:
```json
{
  "chatRoomId": 1,
  "content": "안녕하세요!",
  "type": "TEXT"
}
```

**Broadcast Topic**: `/topic/chatroom/{chatRoomId}`

**Broadcast Message**:
```json
{
  "id": 123,
  "matchId": null,
  "senderProfileId": 1,
  "senderNickname": "발신자",
  "content": "안녕하세요!",
  "type": "TEXT",
  "sentAt": "2024-01-15T10:00:00",
  "isRead": false,
  "readAt": null
}
```

---

### 7. 채팅방 메시지 읽음 처리 (WebSocket)

**Destination**: `/app/chatroom.read`

**Payload**: `chatRoomId` (Long)

**Broadcast Topic**: `/topic/chatroom/{chatRoomId}/read`

**Broadcast Message**: `profileId` (Long) - 메시지를 읽은 사용자의 프로필 ID

---

## 어드민 API

### 8. 모든 매칭된 채팅방 리스트 조회
모든 커플의 채팅방 리스트를 조회합니다.

**Endpoint**: `GET /api/chatrooms/admin/matched`

**Response** (200 OK):
```json
[
  {
    "chatRoomId": 1,
    "matchId": 10,
    "roomType": "MATCHED",
    "otherProfileId": 2,
    "otherNickname": "사용자2",
    "otherMbti": "INFP",
    "createdAt": "2024-01-15T10:00:00",
    "lastMessageAt": "2024-01-15T14:30:00",
    "lastMessage": "안녕하세요!",
    "unreadCount": 15,
    "isActive": true
  },
  {
    "chatRoomId": 3,
    "matchId": 11,
    "roomType": "MATCHED",
    "otherProfileId": 4,
    "otherNickname": "사용자4",
    "otherMbti": "ENTJ",
    "createdAt": "2024-01-14T09:00:00",
    "lastMessageAt": "2024-01-14T18:45:00",
    "lastMessage": "좋은 밤 되세요",
    "unreadCount": 8,
    "isActive": true
  }
]
```

**설명**:
- 어드민은 권한 확인 없이 모든 매칭된 채팅방 조회 가능
- 최근 메시지 시간 순으로 정렬

---

### 9. 특정 채팅방의 모든 메시지 조회 (어드민)
특정 채팅방의 모든 메시지를 조회합니다.

**Endpoint**: `GET /api/chatrooms/admin/{chatRoomId}/messages`

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "matchId": 10,
    "senderProfileId": 1,
    "senderNickname": "사용자1",
    "content": "안녕하세요",
    "type": "TEXT",
    "sentAt": "2024-01-15T10:00:00",
    "isRead": true,
    "readAt": "2024-01-15T10:05:00"
  },
  {
    "id": 2,
    "matchId": 10,
    "senderProfileId": 2,
    "senderNickname": "사용자2",
    "content": "네 안녕하세요!",
    "type": "TEXT",
    "sentAt": "2024-01-15T10:01:00",
    "isRead": true,
    "readAt": "2024-01-15T10:02:00"
  }
]
```

**설명**:
- 어드민은 권한 확인 없이 모든 메시지 열람 가능
- 어드민은 메시지를 보내지는 못함 (읽기 전용)

---

## 데이터 모델

### ChatRoom
```java
{
  "id": Long,
  "profile1": Profile,
  "profile2": Profile,
  "match": Match (nullable),  // 매칭된 경우만
  "roomType": "MATCHED" | "UNMATCHED",
  "isActive": Boolean,
  "createdAt": LocalDateTime,
  "lastMessageAt": LocalDateTime (nullable)
}
```

### RoomType
- `MATCHED`: 매칭된 커플의 채팅방
- `UNMATCHED`: 매칭되지 않은 사용자 간의 채팅방

---

## 사용 시나리오

### 시나리오 1: 프로필 리스트에서 새로운 사용자와 채팅 시작
1. 사용자 A가 프로필 리스트에서 사용자 B를 발견
2. `POST /api/chatrooms`로 채팅방 생성/조회 (targetProfileId: B)
3. 채팅방 ID를 받아 WebSocket으로 메시지 전송
4. `/topic/chatroom/{chatRoomId}` 구독하여 실시간 메시지 수신

### 시나리오 2: 채팅방 리스트에서 기존 대화 이어가기
1. `GET /api/chatrooms`로 채팅방 리스트 조회
2. 원하는 채팅방 선택
3. `GET /api/chatrooms/{chatRoomId}/messages`로 이전 메시지 로드
4. WebSocket으로 실시간 메시지 송수신

### 시나리오 3: 어드민이 커플 채팅 모니터링
1. `GET /api/chatrooms/admin/matched`로 모든 커플 채팅방 조회
2. 특정 채팅방 선택
3. `GET /api/chatrooms/admin/{chatRoomId}/messages`로 메시지 열람

---

## 주요 특징

### 1. 유연한 채팅 시스템
- 매칭 여부와 관계없이 모든 사용자 간 채팅 가능
- 매칭되면 자동으로 채팅방 타입이 "MATCHED"로 변경
- 하나의 통합된 인터페이스로 모든 채팅 관리

### 2. 자동 채팅방 관리
- 채팅방이 없으면 자동 생성
- 중복 채팅방 방지 (같은 두 사용자 간 채팅방은 1개만)
- 마지막 메시지 시간 자동 업데이트

### 3. 읽음 처리
- 채팅방 단위로 일괄 읽음 처리
- 실시간 읽음 알림 (WebSocket)
- 미읽은 메시지 수 자동 계산

### 4. 어드민 모니터링
- 권한 확인 없이 모든 매칭된 채팅방 조회
- 읽기 전용 접근 (메시지 전송 불가)
- 커플 관리 및 모니터링 지원

---

## 에러 처리

### 400 Bad Request
- 자기 자신과 채팅방 생성 시도
- 필수 파라미터 누락

### 403 Forbidden
- 채팅방 접근 권한 없음

### 404 Not Found
- 채팅방을 찾을 수 없음
- 프로필을 찾을 수 없음

---

## WebSocket 연결 예시

```javascript
// SockJS 및 STOMP 클라이언트 초기화
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
  console.log('Connected: ' + frame);
  
  // 채팅방 메시지 구독
  stompClient.subscribe('/topic/chatroom/' + chatRoomId, function(message) {
    const chatMessage = JSON.parse(message.body);
    displayMessage(chatMessage);
  });
  
  // 읽음 알림 구독
  stompClient.subscribe('/topic/chatroom/' + chatRoomId + '/read', function(message) {
    const readerProfileId = JSON.parse(message.body);
    markMessagesAsRead(readerProfileId);
  });
});

// 메시지 전송
function sendMessage(content) {
  stompClient.send("/app/chatroom.send", {}, JSON.stringify({
    chatRoomId: chatRoomId,
    content: content,
    type: 'TEXT'
  }));
}

// 읽음 처리
function markAsRead() {
  stompClient.send("/app/chatroom.read", {}, chatRoomId);
}
```

---

## 보안

- 모든 API는 인증 필요 (JWT 또는 OAuth2)
- 채팅방 접근 시 참여자 여부 확인
- 어드민 API는 별도 권한 체크 필요 (SecurityConfig에서 설정)

---

## 테스트

### cURL 예시

#### 1. 채팅방 생성
```bash
curl -X POST http://localhost:8080/api/chatrooms \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"targetProfileId": 2}'
```

#### 2. 채팅방 리스트 조회
```bash
curl -X GET http://localhost:8080/api/chatrooms \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 3. 어드민 - 모든 매칭 채팅방 조회
```bash
curl -X GET http://localhost:8080/api/chatrooms/admin/matched \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

#### 4. 어드민 - 채팅방 메시지 조회
```bash
curl -X GET http://localhost:8080/api/chatrooms/admin/1/messages \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

