# 채팅 목록 API 문서

## 개요
매칭된 커플 프로필(1개)과 나머지 일반 프로필들을 구분하여 조회하는 API입니다.

## API 엔드포인트

### 채팅 목록 조회
**GET** `/api/chat/list`

매칭된 커플 프로필 1개와 매칭되지 않은 모든 프로필 목록을 조회합니다.

#### 요청
- **인증**: OAuth2 인증 필요
- **헤더**: 
  - `Authorization: Bearer {access_token}`

#### 응답 성공 (200 OK)

```json
{
  "matchedProfile": {
    "matchId": 1,
    "profileId": 2,
    "nickname": "김철수",
    "mbti": "ENFP",
    "matchedAt": "2025-12-07T10:30:00",
    "unreadMessageCount": 3,
    "lastMessage": "안녕하세요!",
    "lastMessageTime": "2025-12-07T14:25:00"
  },
  "otherProfiles": [
    {
      "profileId": 3,
      "nickname": "이영희",
      "mbti": "INFJ",
      "createdAt": "2025-12-05T09:00:00"
    },
    {
      "profileId": 4,
      "nickname": "박민수",
      "mbti": "ISTJ",
      "createdAt": "2025-12-06T11:20:00"
    }
  ]
}
```

#### 응답 필드 설명

##### matchedProfile (매칭된 커플 프로필)
- `matchId` (Long): 매칭 ID
- `profileId` (Long): 상대방 프로필 ID
- `nickname` (String): 상대방 닉네임
- `mbti` (String): 상대방 MBTI
- `matchedAt` (LocalDateTime): 매칭된 시간
- `unreadMessageCount` (Long): 읽지 않은 메시지 수
- `lastMessage` (String): 마지막 메시지 내용 (없으면 null)
- `lastMessageTime` (LocalDateTime): 마지막 메시지 시간 (없으면 null)

**참고**: 매칭이 없는 경우 `matchedProfile`은 `null`입니다.

##### otherProfiles (일반 프로필 목록)
- `profileId` (Long): 프로필 ID
- `nickname` (String): 닉네임
- `mbti` (String): MBTI
- `createdAt` (LocalDateTime): 프로필 생성 시간

## 비즈니스 로직

### 처리 순서
1. 현재 사용자의 활성 매칭 조회
2. 첫 번째 매칭이 있는 경우:
   - 상대방 프로필 정보 추출
   - 최신 메시지 조회
   - 미읽은 메시지 수 계산
3. 모든 프로필 조회 (자신 제외)
4. 매칭된 프로필을 제외한 나머지 프로필만 필터링

### 특징
- 커플은 최대 1개만 반환 (첫 번째 매칭)
- 자신의 프로필은 목록에서 제외
- 매칭된 프로필은 일반 프로필 목록에서 제외
- 실시간 미읽은 메시지 수 제공
- 마지막 메시지 미리보기 제공

## 사용 예시

### cURL
```bash
curl -X GET "http://localhost:8080/api/chat/list" \
  -H "Authorization: Bearer {your_access_token}" \
  -H "Content-Type: application/json"
```

### JavaScript (Fetch API)
```javascript
fetch('http://localhost:8080/api/chat/list', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => {
  console.log('매칭된 커플:', data.matchedProfile);
  console.log('일반 프로필:', data.otherProfiles);
})
.catch(error => console.error('Error:', error));
```

## 관련 API

### 기존 매칭 목록 API
**GET** `/api/chat/matches`

모든 활성 매칭 목록을 조회합니다 (복수 매칭 지원).

```json
[
  {
    "matchId": 1,
    "otherProfile": {
      "id": 2,
      "nickname": "김철수",
      "mbti": "ENFP"
    },
    "unreadCount": 3,
    "matchedAt": "2025-12-07T10:30:00"
  }
]
```

## 에러 응답

### 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "인증이 필요합니다."
}
```

### 404 Not Found
```json
{
  "error": "Not Found",
  "message": "프로필을 찾을 수 없습니다."
}
```

## 데이터베이스 쿼리

### 사용된 주요 쿼리
1. **활성 매칭 조회**: `MatchRepository.findActiveMatchesByProfileId()`
2. **최신 메시지 조회**: `ChatMessageRepository.findLatestMessageByMatchId()`
3. **미읽은 메시지 수**: `ChatMessageRepository.countUnreadMessagesByMatchIdAndNotSender()`
4. **전체 프로필 조회**: `ProfileRepository.findAllExcept()`

## 아키텍처

```
ChatController (프레젠테이션 계층)
    ↓
ChatService (비즈니스 로직 계층)
    ↓
MatchRepository, ChatMessageRepository, ProfileRepository (데이터 접근 계층)
    ↓
Match, ChatMessage, Profile (도메인 엔티티)
```

## 테스트

### 매칭이 있는 경우
```bash
# 매칭 생성 (테스트용)
curl -X POST "http://localhost:8080/api/chat/matches?targetProfileId=2" \
  -H "Authorization: Bearer {your_access_token}"

# 채팅 목록 조회
curl -X GET "http://localhost:8080/api/chat/list" \
  -H "Authorization: Bearer {your_access_token}"
```

### 매칭이 없는 경우
```json
{
  "matchedProfile": null,
  "otherProfiles": [
    {
      "profileId": 2,
      "nickname": "김철수",
      "mbti": "ENFP",
      "createdAt": "2025-12-05T09:00:00"
    },
    {
      "profileId": 3,
      "nickname": "이영희",
      "mbti": "INFJ",
      "createdAt": "2025-12-06T11:20:00"
    }
  ]
}
```

## 주의사항

1. **커플 제한**: 현재는 첫 번째 매칭만 반환합니다. 복수 매칭이 필요한 경우 `/api/chat/matches` API를 사용하세요.
2. **성능**: 프로필 수가 많을 경우 페이징 처리가 필요할 수 있습니다.
3. **실시간 업데이트**: WebSocket 연결 시 실시간으로 미읽은 메시지 수가 변경됩니다.
4. **인증 필수**: 모든 요청에는 유효한 OAuth2 액세스 토큰이 필요합니다.

## 향후 개선 사항

- [ ] 프로필 목록 페이징 처리
- [ ] 프로필 필터링 (MBTI, 키워드 등)
- [ ] 프로필 정렬 옵션 (최신순, 이름순 등)
- [ ] 복수 커플 지원 (현재는 1개만)
- [ ] 프로필 검색 기능

