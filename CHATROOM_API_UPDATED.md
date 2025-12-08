# Transit Love - 채팅방 API 문서 (업데이트)

## 📌 개요
매칭 여부와 관계없이 두 프로필 간 채팅방을 생성하고 관리하는 API입니다.
- **MATCHED 채팅방**: 매칭된 커플의 채팅방 (만료 없음)
- **UNMATCHED 채팅방**: 매칭되지 않은 채팅방 (생성 후 2시간 후 만료)

## 🚀 주요 변경사항
1. ✅ 채팅방 생성/조회 API - `POST /api/chatrooms`
2. ✅ 채팅방 리스트 조회 API - `GET /api/chatrooms`
3. ✅ 채팅방 메시지 조회 API - `GET /api/chatrooms/{chatRoomId}/messages`
4. ✅ **만료 시간 (expiresAt)** 추가 - UNMATCHED 채팅방만 해당 (생성 시간 + 2시간)
5. ✅ **410 Gone** 응답 - 만료된 채팅방 접근 시

---

## 📡 API 엔드포인트

### 1. 채팅방 생성/조회

#### 요청
```http
POST /api/chatrooms
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}

{
  "targetProfileId": 2
}
```

#### 응답 (성공 - 200 OK)
```json
{
  "chatRoomId": 1,
  "matchId": null,
  "roomType": "UNMATCHED",
  "otherProfileId": 2,
  "otherNickname": "민수",
  "otherMbti": "ENFP",
  "createdAt": "2024-12-08T10:00:00",
  "lastMessageAt": null,
  "expiresAt": "2024-12-08T12:00:00",
  "isActive": true
}
```

**특징:**
- 기존 채팅방이 있으면 조회, 없으면 새로 생성
- UNMATCHED 타입은 `expiresAt` 포함 (생성 시간 + 2시간)
- MATCHED 타입은 `expiresAt`이 null (만료 없음)

---

### 2. 채팅방 리스트 조회

#### 요청
```http
GET /api/chatrooms
Authorization: Bearer {JWT_TOKEN}
```

#### 응답 (성공 - 200 OK)
```json
{
  "matchedRooms": [
    {
      "chatRoomId": 5,
      "matchId": 3,
      "roomType": "MATCHED",
      "otherProfileId": 4,
      "otherNickname": "지수",
      "otherMbti": "INFJ",
      "createdAt": "2024-12-07T15:30:00",
      "lastMessageAt": "2024-12-08T09:45:00",
      "expiresAt": null,
      "lastMessage": "안녕하세요!",
      "unreadCount": 2,
      "isActive": true
    }
  ],
  "unmatchedRooms": [
    {
      "chatRoomId": 1,
      "matchId": null,
      "roomType": "UNMATCHED",
      "otherProfileId": 2,
      "otherNickname": "민수",
      "otherMbti": "ENFP",
      "createdAt": "2024-12-08T10:00:00",
      "lastMessageAt": "2024-12-08T10:15:00",
      "expiresAt": "2024-12-08T12:00:00",
      "lastMessage": "반가워요",
      "unreadCount": 0,
      "isActive": true
    }
  ],
  "totalCount": 2
}
```

**특징:**
- `matchedRooms`: 매칭된 채팅방 목록 (expiresAt = null)
- `unmatchedRooms`: 매칭되지 않은 채팅방 목록 (expiresAt 포함)
- 각 채팅방에 마지막 메시지, 미읽음 수 포함

---

### 3. 채팅방 메시지 조회

#### 요청
```http
GET /api/chatrooms/{chatRoomId}/messages
Authorization: Bearer {JWT_TOKEN}
```

#### 응답 (성공 - 200 OK)
```json
[
  {
    "id": 1,
    "chatRoomId": 1,
    "matchId": null,
    "senderProfileId": 1,
    "senderNickname": "나",
    "content": "안녕하세요!",
    "type": "TEXT",
    "isRead": true,
    "sentAt": "2024-12-08T10:10:00",
    "readAt": "2024-12-08T10:11:00"
  },
  {
    "id": 2,
    "chatRoomId": 1,
    "matchId": null,
    "senderProfileId": 2,
    "senderNickname": "민수",
    "content": "반가워요",
    "type": "TEXT",
    "isRead": false,
    "sentAt": "2024-12-08T10:15:00",
    "readAt": null
  }
]
```

#### 응답 (만료된 채팅방 - 410 Gone)
```json
{
  "timestamp": "2024-12-08T12:05:00",
  "status": 410,
  "error": "Gone",
  "message": "채팅방이 만료되었습니다. 채팅방 ID: 1"
}
```

**특징:**
- 만료된 UNMATCHED 채팅방 접근 시 **410 Gone** 반환
- MATCHED 채팅방은 만료되지 않음

---

### 4. 채팅방 메시지 읽음 처리

#### 요청
```http
PUT /api/chatrooms/{chatRoomId}/read
Authorization: Bearer {JWT_TOKEN}
```

#### 응답 (성공 - 200 OK)
```json
"메시지가 읽음 처리되었습니다."
```

#### 응답 (만료된 채팅방 - 410 Gone)
```json
{
  "timestamp": "2024-12-08T12:05:00",
  "status": 410,
  "error": "Gone",
  "message": "채팅방이 만료되었습니다. 채팅방 ID: 1"
}
```

---

### 5. 채팅방 비활성화

#### 요청
```http
DELETE /api/chatrooms/{chatRoomId}
Authorization: Bearer {JWT_TOKEN}
```

#### 응답 (성공 - 200 OK)
```json
"채팅방이 비활성화되었습니다."
```

---

## 🔐 만료 정책

### UNMATCHED 채팅방
- 생성 시간 + **2시간 후 자동 만료**
- 만료 후 접근 시 **410 Gone** 반환
- `expiresAt` 필드에 만료 시간 포함

### MATCHED 채팅방
- **만료되지 않음**
- `expiresAt` = null

---

## 📊 데이터 구조

### ChatRoom 엔티티 (업데이트)
```java
@Entity
public class ChatRoom {
    private Long id;
    private Profile profile1;
    private Profile profile2;
    private Match match;              // 매칭 정보 (있으면 MATCHED)
    private RoomType roomType;        // MATCHED | UNMATCHED
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
    private LocalDateTime expiresAt;  // ⭐ NEW: 만료 시간 (UNMATCHED만)
}
```

---

## 🧪 테스트 시나리오

### 1. UNMATCHED 채팅방 생성 및 만료 테스트
```bash
# 1. 채팅방 생성
curl -X POST http://localhost:8080/api/chatrooms \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -d '{"targetProfileId": 2}'

# 응답: expiresAt = 생성시간 + 2시간

# 2. 메시지 전송 (2시간 이내)
curl -X GET http://localhost:8080/api/chatrooms/1/messages \
  -H "Authorization: Bearer {JWT_TOKEN}"

# 응답: 200 OK

# 3. 2시간 후 접근
curl -X GET http://localhost:8080/api/chatrooms/1/messages \
  -H "Authorization: Bearer {JWT_TOKEN}"

# 응답: 410 Gone
```

### 2. MATCHED 채팅방 (만료 없음)
```bash
# 1. 매칭 후 채팅방 생성
curl -X POST http://localhost:8080/api/chatrooms \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -d '{"targetProfileId": 3}'

# 응답: roomType = "MATCHED", expiresAt = null

# 2. 언제든지 접근 가능
curl -X GET http://localhost:8080/api/chatrooms/5/messages \
  -H "Authorization: Bearer {JWT_TOKEN}"

# 응답: 200 OK (항상)
```

---

## ✅ 구현 완료 체크리스트

- [x] ChatRoom 엔티티에 `expiresAt` 필드 추가
- [x] UNMATCHED 생성 시 자동으로 2시간 후 만료 시간 설정
- [x] MATCHED로 변경 시 `expiresAt` 제거
- [x] `isExpired()` 메서드 구현
- [x] ChatRoomResponse에 `expiresAt` 추가
- [x] ChatRoomListResponse.ChatRoomDto에 `expiresAt` 추가
- [x] ChatRoomExpiredException 생성 (410 Gone)
- [x] GlobalExceptionHandler 생성
- [x] findChatRoomWithPermission에 만료 확인 로직 추가
- [x] API 문서 업데이트

---

## 🚀 사용 예제

### JavaScript/React
```javascript
// 채팅방 생성/조회
const createOrGetChatRoom = async (targetProfileId) => {
  try {
    const response = await axios.post('/api/chatrooms', {
      targetProfileId
    }, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    
    const { chatRoomId, roomType, expiresAt } = response.data;
    
    // UNMATCHED면 만료 시간 체크
    if (roomType === 'UNMATCHED' && expiresAt) {
      const expiresDate = new Date(expiresAt);
      console.log('채팅방 만료 시간:', expiresDate);
      
      // 타이머 설정
      setTimeout(() => {
        alert('채팅방이 만료되었습니다.');
      }, expiresDate - new Date());
    }
    
    return response.data;
  } catch (error) {
    if (error.response?.status === 410) {
      alert('채팅방이 만료되었습니다.');
    }
  }
};

// 채팅방 리스트 조회
const getChatRoomList = async () => {
  const response = await axios.get('/api/chatrooms', {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  
  const { matchedRooms, unmatchedRooms } = response.data;
  
  // UNMATCHED 채팅방에서 만료 시간 표시
  unmatchedRooms.forEach(room => {
    if (room.expiresAt) {
      const remaining = new Date(room.expiresAt) - new Date();
      console.log(`채팅방 ${room.chatRoomId} 남은 시간: ${Math.floor(remaining / 60000)}분`);
    }
  });
  
  return response.data;
};
```

---

## 📝 주의사항

1. **만료 시간**: UNMATCHED 채팅방은 생성 후 **정확히 2시간**
2. **410 Gone**: 만료된 채팅방 접근 시 항상 410 반환
3. **MATCHED 전환**: 매칭 성공 시 자동으로 MATCHED로 변경되고 만료 시간 제거
4. **프론트엔드**: `expiresAt`를 확인하여 UI에 남은 시간 표시 권장

---

## 🎯 결론

모든 요구사항이 구현되었습니다:
- ✅ POST /api/chatrooms - 채팅방 생성/조회 (expiresAt 포함)
- ✅ GET /api/chatrooms - 채팅방 리스트 (MATCHED, UNMATCHED 구분)
- ✅ GET /api/chatrooms/{chatRoomId}/messages - 메시지 조회
- ✅ 만료된 채팅방 접근 시 410 Gone 반환
- ✅ UNMATCHED는 2시간 후 자동 만료, MATCHED는 만료 없음

