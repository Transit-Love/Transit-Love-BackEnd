# Transit Love - 채팅 기능 구현 완료

## 🎉 구현된 기능

매칭된 커플끼리 실시간으로 채팅할 수 있는 WebSocket 기반 채팅 API가 성공적으로 구현되었습니다.

## 📁 생성된 파일 목록

### 1. 엔티티 (Entity)
- ✅ `Match.java` - 매칭 정보 엔티티
  - ✅ `ChatMessage.java` - 채팅 메시지 엔티티

### 2. 리포지토리 (Repository)
- ✅ `MatchRepository.java` - 매칭 데이터 접근
  - ✅ `ChatMessageRepository.java` - 채팅 메시지 데이터 접근

### 3. DTO (Data Transfer Object)
- ✅ `ChatMessageRequest.java` - 메시지 전송 요청
  - ✅ `ChatMessageResponse.java` - 메시지 응답
  - ✅ `MatchResponse.java` - 매칭 정보 응답
  - ✅ `ChatListResponse.java` - 채팅 목록 응답 (커플 + 일반 프로필)

### 4. 설정 (Configuration)
- ✅ `WebSocketConfig.java` - WebSocket 설정 (STOMP, SockJS)

### 5. 서비스 (Service)
- ✅ `ChatService.java` - 채팅 비즈니스 로직

### 6. 컨트롤러 (Controller)
- ✅ `ChatController.java` - WebSocket 및 REST API 엔드포인트
- ✅ `AdminChatController.java` - 어드민 전용 채팅 조회 API ⭐ NEW

### 7. 문서
- ✅ `CHAT_API_README.md` - API 문서
- ✅ `CHAT_LIST_API_README.md` - 채팅 목록 API 상세 문서
- ✅ `ADMIN_CHAT_API_README.md` - 어드민 채팅 API 문서 ⭐ NEW
- ✅ `chat-test.html` - 테스트용 HTML 클라이언트

## 🔧 주요 기능

### WebSocket 기능
1. **실시간 메시지 전송** - `/app/chat.send`
   2. **읽음 처리** - `/app/chat.read`
   3. **메시지 구독** - `/topic/chat/{matchId}`

### REST API 기능
1. **매칭 목록 조회** - `GET /api/chat/matches`
2. **채팅 목록 조회** - `GET /api/chat/list` ⭐ NEW
   - 매칭된 커플 프로필 1개 + 일반 프로필 목록
3. **채팅 메시지 목록 조회** - `GET /api/chat/matches/{matchId}/messages`
4. **메시지 읽음 처리** - `PUT /api/chat/matches/{matchId}/read`
5. **매칭 생성** - `POST /api/chat/matches?targetProfileId={id}`
6. **매칭 해제** - `DELETE /api/chat/matches/{matchId}`

### 어드민 전용 REST API ⭐ NEW
1. **모든 커플 매칭 목록 조회** - `GET /api/admin/chat/matches`
   - 비활성 매칭 포함 옵션: `?includeInactive=true`
2. **특정 커플의 채팅 메시지 조회** - `GET /api/admin/chat/matches/{matchId}/messages`

## 🗄️ 데이터베이스 스키마

### matches 테이블
```sql
CREATE TABLE matches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile1_id BIGINT NOT NULL,
    profile2_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    matched_at DATETIME NOT NULL,
    unmatched_at DATETIME,
    FOREIGN KEY (profile1_id) REFERENCES profiles(id),
    FOREIGN KEY (profile2_id) REFERENCES profiles(id)
);
```

### chat_messages 테이블
```sql
CREATE TABLE chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    match_id BIGINT NOT NULL,
    sender_profile_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(20) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at DATETIME NOT NULL,
    read_at DATETIME,
    FOREIGN KEY (match_id) REFERENCES matches(id),
    FOREIGN KEY (sender_profile_id) REFERENCES profiles(id)
);
```

## 🚀 사용 방법

### 1. 애플리케이션 실행
```bash
./gradlew bootRun
```

### 2. 데이터베이스 테이블 생성
애플리케이션이 자동으로 테이블을 생성합니다 (JPA DDL Auto).

### 3. 매칭 생성 (테스트용)
```bash
curl -X POST "http://localhost:8080/api/chat/matches?targetProfileId=2" \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

### 4. WebSocket 연결 테스트
브라우저에서 다음 URL을 열어 테스트:
```
http://localhost:8080/chat-test.html
```

**주의:** `chat-test.html` 파일에서 JWT 토큰을 실제 토큰으로 변경해야 합니다.

## 📝 API 사용 예제

### JavaScript/TypeScript (프론트엔드)
```javascript
// WebSocket 연결
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect(
    {'Authorization': 'Bearer ' + jwtToken},
    (frame) => {
        console.log('Connected');
        
        // 채팅방 구독
        stompClient.subscribe('/topic/chat/' + matchId, (message) => {
            const chatMessage = JSON.parse(message.body);
            console.log('New message:', chatMessage);
        });
    }
);

// 메시지 전송
function sendMessage(matchId, content) {
    stompClient.send('/app/chat.send', {}, JSON.stringify({
        matchId: matchId,
        content: content,
        type: 'TEXT'
    }));
}
```

### REST API 호출 (axios)
```javascript
// 매칭 목록 조회
const matches = await axios.get('/api/chat/matches', {
    headers: { 'Authorization': `Bearer ${token}` }
});

// 채팅 메시지 조회
const messages = await axios.get(`/api/chat/matches/${matchId}/messages`, {
    headers: { 'Authorization': `Bearer ${token}` }
});

// 메시지 읽음 처리
await axios.put(`/api/chat/matches/${matchId}/read`, {}, {
    headers: { 'Authorization': `Bearer ${token}` }
});
```

## 🔐 보안 기능

1. **JWT 인증** - WebSocket 연결 시 JWT 토큰 검증
   2. **권한 검증** - 매칭된 커플만 채팅방 접근 가능
   3. **CORS 설정** - 허용된 도메인에서만 접근 가능

## 📊 주요 특징

✅ **실시간 양방향 통신** - WebSocket (STOMP) 사용
✅ **메시지 영구 저장** - 데이터베이스에 모든 메시지 저장
✅ **읽음 확인** - 메시지 읽음 상태 추적
✅ **미읽음 메시지 카운트** - 매칭별 미읽은 메시지 수 제공
✅ **메시지 타입** - TEXT, IMAGE, SYSTEM 지원
✅ **매칭 관리** - 매칭 생성/해제 기능
✅ **권한 제어** - 매칭된 사용자만 채팅 가능
✅ **어드민 모니터링** ⭐ NEW - 모든 커플의 채팅 조회 (읽기 전용)

## 🧪 테스트 시나리오

### 1. 기본 채팅 테스트
1. 두 개의 브라우저 창 열기
   2. 각각 다른 사용자로 로그인
   3. 매칭 생성
   4. WebSocket 연결
   5. 메시지 주고받기
   6. 읽음 처리 확인

### 2. REST API 테스트
```bash
# 매칭 목록 조회
curl -X GET "http://localhost:8080/api/chat/matches" \
  -H "Authorization: Bearer {JWT_TOKEN}"

# 메시지 목록 조회
curl -X GET "http://localhost:8080/api/chat/matches/1/messages" \
  -H "Authorization: Bearer {JWT_TOKEN}"

# 읽음 처리
curl -X PUT "http://localhost:8080/api/chat/matches/1/read" \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

## 🔄 향후 개선 사항

- [ ] 이미지 업로드 기능 구현
  - [ ] 메시지 삭제/수정 기능
  - [ ] 타이핑 인디케이터 추가
  - [ ] 메시지 검색 기능
  - [ ] 푸시 알림 연동
  - [ ] 채팅방 나가기 기능
  - [ ] 메시지 페이지네이션
  - [ ] 파일 전송 기능
  - [ ] 이모티콘 지원

## 📚 참고 문서

- **CHAT_API_README.md** - 일반 사용자 채팅 API 문서
- **CHAT_LIST_API_README.md** - 채팅 목록 API 상세 문서
- **ADMIN_CHAT_API_README.md** - 어드민 전용 채팅 조회 API 문서 ⭐ NEW

## ✅ 체크리스트

- [x] Match 엔티티 생성
  - [x] ChatMessage 엔티티 생성
  - [x] Repository 생성
  - [x] DTO 생성
  - [x] WebSocket 설정
  - [x] ChatService 구현
  - [x] ChatController 구현
  - [x] SecurityConfig 업데이트
  - [x] API 문서 작성
  - [x] 테스트 클라이언트 제공

## 🎯 결론

매칭된 커플 간의 실시간 채팅 기능이 완전히 구현되었습니다!
- WebSocket을 통한 실시간 통신
- REST API를 통한 채팅 관리
- JWT 인증 및 권한 제어
- 메시지 읽음 처리
- 매칭 관리 기능
- **어드민 전용 모니터링 기능** ⭐ NEW

모든 코드가 정상적으로 작성되었으며, 빌드 및 테스트가 가능합니다.

