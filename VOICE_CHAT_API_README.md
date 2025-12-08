# 🎤 음성 채팅 API 문서

## 📋 개요

실시간 음성 채팅 기능을 제공하는 API입니다. WebRTC를 활용하여 P2P 음성 통신을 지원하며, 서버는 시그널링(Signaling) 역할만 수행합니다.

**음성 변조는 클라이언트에서 처리**하며, 서버는 WebRTC 연결을 위한 SDP(Session Description Protocol) 및 ICE Candidate 교환만 담당합니다.

## 🏗️ 아키텍처

```
┌─────────────┐                    ┌─────────────┐
│  Client A   │                    │  Client B   │
│  (발신자)    │                    │  (수신자)    │
└──────┬──────┘                    └──────┬──────┘
       │                                  │
       │    1. VOICE_CALL_REQUEST        │
       ├─────────────────────────────────>│
       │                                  │
       │    2. VOICE_CALL_ACCEPT         │
       │<─────────────────────────────────┤
       │                                  │
       │    3. OFFER (WebRTC SDP)        │
       ├─────────────────────────────────>│
       │                                  │
       │    4. ANSWER (WebRTC SDP)       │
       │<─────────────────────────────────┤
       │                                  │
       │    5. ICE Candidates            │
       │<────────────────────────────────>│
       │                                  │
       │    6. Direct Audio Stream       │
       │ ═══════════════════════════════> │
       │         (P2P Connection)         │
       │                                  │
       │    7. VOICE_CALL_END            │
       ├─────────────────────────────────>│
       │                                  │
```

## 🔧 기술 스택

- **WebSocket (STOMP)**: 시그널링 메시지 전송
- **WebRTC**: P2P 음성 통신
- **Spring Boot**: 백엔드 프레임워크
- **SockJS**: WebSocket fallback

## 📡 WebSocket 연결

### 엔드포인트
```
ws://localhost:8080/ws
```

### 연결 헤더
```json
{
  "Authorization": "Bearer {JWT_TOKEN}"
}
```

### 구독 경로
```
/queue/voice.{profileId}
```
각 사용자는 자신의 프로필 ID로 큐를 구독하여 음성 시그널을 수신합니다.

### 발행 경로
```
/app/voice/signal
```

## 📨 메시지 타입

### VoiceSignalType

| 타입 | 설명 | 발신자 | 수신자 |
|------|------|--------|--------|
| `VOICE_CALL_REQUEST` | 음성 통화 요청 | 발신자 | 수신자 |
| `VOICE_CALL_ACCEPT` | 음성 통화 수락 | 수신자 | 발신자 |
| `VOICE_CALL_REJECT` | 음성 통화 거절 | 수신자 | 발신자 |
| `VOICE_CALL_END` | 음성 통화 종료 | 양쪽 | 상대방 |
| `VOICE_CALL_CANCEL` | 음성 통화 취소 | 발신자 | 수신자 |
| `OFFER` | WebRTC Offer | 발신자 | 수신자 |
| `ANSWER` | WebRTC Answer | 수신자 | 발신자 |
| `ICE_CANDIDATE` | ICE Candidate | 양쪽 | 상대방 |
| `ERROR` | 에러 메시지 | 서버 | 클라이언트 |

## 🎯 API 엔드포인트

### 1. 음성 채팅방 정보 조회

```http
GET /api/voice/room/{chatRoomId}
```

**Headers:**
```
Authorization: Bearer {JWT_TOKEN}
```

**Response:**
```json
{
  "chatRoomId": 1,
  "myProfileId": 1,
  "partnerProfileId": 2,
  "partnerNickname": "김철수",
  "voiceEnabled": true,
  "createdAt": "2024-01-01T12:00:00"
}
```

### 2. 음성 통화 강제 종료

```http
POST /api/voice/room/{chatRoomId}/end
```

**Headers:**
```
Authorization: Bearer {JWT_TOKEN}
```

**Response:**
```
200 OK
```

### 3. 활성 세션 상태 조회

```http
GET /api/voice/room/{chatRoomId}/status
```

**Headers:**
```
Authorization: Bearer {JWT_TOKEN}
```

**Response (활성):**
```json
{
  "active": true,
  "session": {
    "chatRoomId": 1,
    "callerId": 1,
    "receiverId": 2,
    "startTime": "2024-01-01T12:00:00",
    "accepted": true
  }
}
```

**Response (비활성):**
```json
{
  "active": false,
  "session": null
}
```

## 📤 WebSocket 메시지 예제

### 1. 음성 통화 요청 (VOICE_CALL_REQUEST)

**발신:**
```json
{
  "type": "VOICE_CALL_REQUEST",
  "chatRoomId": 1,
  "fromProfileId": 1,
  "toProfileId": 2,
  "timestamp": 1704096000000
}
```

**수신자에게 전달:**
```json
{
  "type": "VOICE_CALL_REQUEST",
  "chatRoomId": 1,
  "fromProfileId": 1,
  "toProfileId": 2,
  "timestamp": 1704096000000
}
```

### 2. 음성 통화 수락 (VOICE_CALL_ACCEPT)

**발신:**
```json
{
  "type": "VOICE_CALL_ACCEPT",
  "chatRoomId": 1,
  "fromProfileId": 2,
  "toProfileId": 1,
  "timestamp": 1704096005000
}
```

### 3. WebRTC Offer

**발신:**
```json
{
  "type": "OFFER",
  "chatRoomId": 1,
  "fromProfileId": 1,
  "toProfileId": 2,
  "sdp": "v=0\r\no=- 123456789 2 IN IP4 127.0.0.1\r\n...",
  "timestamp": 1704096010000
}
```

### 4. WebRTC Answer

**발신:**
```json
{
  "type": "ANSWER",
  "chatRoomId": 1,
  "fromProfileId": 2,
  "toProfileId": 1,
  "sdp": "v=0\r\no=- 987654321 2 IN IP4 127.0.0.1\r\n...",
  "timestamp": 1704096015000
}
```

### 5. ICE Candidate

**발신:**
```json
{
  "type": "ICE_CANDIDATE",
  "chatRoomId": 1,
  "fromProfileId": 1,
  "toProfileId": 2,
  "iceCandidate": {
    "candidate": "candidate:1 1 UDP 2122260223 192.168.1.100 54321 typ host",
    "sdpMid": "audio",
    "sdpMLineIndex": 0
  },
  "timestamp": 1704096020000
}
```

### 6. 음성 통화 종료 (VOICE_CALL_END)

**발신:**
```json
{
  "type": "VOICE_CALL_END",
  "chatRoomId": 1,
  "fromProfileId": 1,
  "toProfileId": 2,
  "timestamp": 1704096120000
}
```

### 7. 에러 메시지 (ERROR)

**서버에서 수신:**
```json
{
  "type": "ERROR",
  "chatRoomId": 1,
  "toProfileId": 1,
  "errorMessage": "이미 진행 중인 통화가 있습니다.",
  "timestamp": 1704096000000
}
```

## 🔄 통화 플로우

### 정상 통화 플로우

```
1. A → B: VOICE_CALL_REQUEST (통화 요청)
2. B → A: VOICE_CALL_ACCEPT (통화 수락)
3. A → B: OFFER (WebRTC SDP)
4. B → A: ANSWER (WebRTC SDP)
5. A ↔ B: ICE_CANDIDATE (여러 개 교환)
6. [P2P 음성 통신 시작]
7. A/B → B/A: VOICE_CALL_END (통화 종료)
```

### 통화 거절 플로우

```
1. A → B: VOICE_CALL_REQUEST (통화 요청)
2. B → A: VOICE_CALL_REJECT (통화 거절)
```

### 통화 취소 플로우

```
1. A → B: VOICE_CALL_REQUEST (통화 요청)
2. A → B: VOICE_CALL_CANCEL (통화 취소)
```

## 🎨 클라이언트 구현 가이드

### 1. WebSocket 연결

```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

const headers = {
  'Authorization': 'Bearer ' + jwtToken
};

stompClient.connect(headers, function(frame) {
  // 내 큐 구독
  stompClient.subscribe('/queue/voice.' + myProfileId, function(message) {
    handleVoiceSignal(JSON.parse(message.body));
  });
});
```

### 2. 통화 요청 전송

```javascript
function startCall(chatRoomId, partnerProfileId) {
  const message = {
    type: 'VOICE_CALL_REQUEST',
    chatRoomId: chatRoomId,
    fromProfileId: myProfileId,
    toProfileId: partnerProfileId,
    timestamp: Date.now()
  };
  
  stompClient.send('/app/voice/signal', {}, JSON.stringify(message));
}
```

### 3. WebRTC 설정

```javascript
const rtcConfig = {
  iceServers: [
    { urls: 'stun:stun.l.google.com:19302' },
    { urls: 'stun:stun1.l.google.com:19302' }
  ]
};

const peerConnection = new RTCPeerConnection(rtcConfig);

// 로컬 오디오 스트림 획득
navigator.mediaDevices.getUserMedia({ audio: true })
  .then(stream => {
    localStream = stream;
    
    // 음성 변조 적용 (클라이언트에서 처리)
    const audioContext = new AudioContext();
    const source = audioContext.createMediaStreamSource(stream);
    const pitchShifter = audioContext.createBiquadFilter();
    // ... 음성 변조 로직 ...
    
    // 변조된 스트림을 PeerConnection에 추가
    stream.getTracks().forEach(track => {
      peerConnection.addTrack(track, stream);
    });
  });
```

### 4. Offer 생성 및 전송

```javascript
peerConnection.createOffer()
  .then(offer => {
    return peerConnection.setLocalDescription(offer);
  })
  .then(() => {
    const message = {
      type: 'OFFER',
      chatRoomId: chatRoomId,
      fromProfileId: myProfileId,
      toProfileId: partnerProfileId,
      sdp: peerConnection.localDescription.sdp,
      timestamp: Date.now()
    };
    
    stompClient.send('/app/voice/signal', {}, JSON.stringify(message));
  });
```

### 5. ICE Candidate 처리

```javascript
peerConnection.onicecandidate = (event) => {
  if (event.candidate) {
    const message = {
      type: 'ICE_CANDIDATE',
      chatRoomId: chatRoomId,
      fromProfileId: myProfileId,
      toProfileId: partnerProfileId,
      iceCandidate: {
        candidate: event.candidate.candidate,
        sdpMid: event.candidate.sdpMid,
        sdpMLineIndex: event.candidate.sdpMLineIndex
      },
      timestamp: Date.now()
    };
    
    stompClient.send('/app/voice/signal', {}, JSON.stringify(message));
  }
};
```

## 🎙️ 음성 변조 예제 (클라이언트)

### Pitch Shifting (음높이 변경)

```javascript
const audioContext = new AudioContext();
const source = audioContext.createMediaStreamSource(stream);
const destination = audioContext.createMediaStreamDestination();

// Biquad Filter로 간단한 음높이 변경
const filter = audioContext.createBiquadFilter();
filter.type = 'allpass';
filter.frequency.value = 1000; // Hz

source.connect(filter);
filter.connect(destination);

// 변조된 스트림 사용
const modifiedStream = destination.stream;
```

### Voice Distortion (음성 왜곡)

```javascript
// WaveShaper로 음성 왜곡
const distortion = audioContext.createWaveShaper();
distortion.curve = makeDistortionCurve(400);
distortion.oversample = '4x';

source.connect(distortion);
distortion.connect(destination);
```

### Echo Effect (에코 효과)

```javascript
// Delay로 에코 효과
const delay = audioContext.createDelay();
delay.delayTime.value = 0.3; // 300ms 딜레이

const feedback = audioContext.createGain();
feedback.gain.value = 0.4;

source.connect(delay);
delay.connect(feedback);
feedback.connect(delay);
delay.connect(destination);
```

## 🔐 보안

- **JWT 인증**: WebSocket 연결 시 JWT 토큰 필수
- **권한 검증**: 채팅방 참여자만 음성 통화 가능
- **세션 관리**: 서버에서 활성 세션 관리 및 검증

## ⚡ 성능 최적화

- **P2P 연결**: 음성 데이터는 서버를 거치지 않고 직접 전송
- **경량 시그널링**: 서버는 시그널링 메시지만 전달
- **WebSocket**: 실시간 양방향 통신으로 지연 최소화

## 🧪 테스트

### 테스트 페이지

```
http://localhost:8080/voice-chat-test.html
```

### 테스트 시나리오

1. **기본 연결 테스트**
   - JWT 토큰 입력
   - WebSocket 연결
   - 큐 구독 확인

2. **통화 요청/수락 테스트**
   - 2개의 브라우저 탭 열기
   - A → B 통화 요청
   - B에서 수락
   - WebRTC 연결 확인

3. **음성 변조 테스트**
   - WebRTC 테스트 버튼 클릭
   - 마이크 권한 허용
   - AudioContext 로드 확인

4. **통화 종료 테스트**
   - 통화 중 종료 버튼 클릭
   - 양쪽 연결 해제 확인

## 🐛 에러 처리

### 주요 에러 케이스

| 에러 메시지 | 원인 | 해결 방법 |
|------------|------|----------|
| "이미 진행 중인 통화가 있습니다" | 채팅방에 활성 세션 존재 | 기존 통화 종료 후 재시도 |
| "채팅방을 찾을 수 없습니다" | 잘못된 채팅방 ID | 올바른 채팅방 ID 확인 |
| "해당 채팅방에 접근 권한이 없습니다" | 채팅방 참여자 아님 | 권한 확인 |
| "통화 요청을 찾을 수 없습니다" | 만료된 세션 | 새로 통화 요청 |

### 클라이언트 에러 처리

```javascript
function handleVoiceSignal(message) {
  if (message.type === 'ERROR') {
    console.error('음성 채팅 에러:', message.errorMessage);
    alert(message.errorMessage);
    // 에러 처리 로직
  }
}
```

## 📊 모니터링

### 로그 확인

```bash
# 서버 로그에서 음성 채팅 관련 로그 확인
tail -f server.log | grep "음성"
```

### 주요 로그 메시지

```
[INFO] 음성 채팅방 정보 조회 - 채팅방 ID: 1, 프로필 ID: 1
[INFO] 음성 통화 요청 - 채팅방: 1, 발신자: 1, 수신자: 2
[INFO] 음성 통화 수락 - 채팅방: 1, 수락자: 2
[INFO] WebRTC Offer 전달 - 채팅방: 1
[INFO] WebRTC Answer 전달 - 채팅방: 1
[DEBUG] ICE Candidate 전달 - 채팅방: 1
[INFO] 음성 통화 종료 - 채팅방: 1, 종료자: 1
```

## 🚀 배포 시 고려사항

### STUN/TURN 서버

프로덕션 환경에서는 자체 TURN 서버 구축 권장:

```javascript
const rtcConfig = {
  iceServers: [
    { urls: 'stun:your-stun-server.com:3478' },
    {
      urls: 'turn:your-turn-server.com:3478',
      username: 'username',
      credential: 'password'
    }
  ]
};
```

### HTTPS 필수

- WebRTC는 HTTPS 환경 필요 (localhost 제외)
- SSL/TLS 인증서 설정

### 방화벽 설정

- UDP 포트 개방 (STUN/TURN)
- WebSocket 포트 개방

## 📝 참고 자료

- [WebRTC 공식 문서](https://webrtc.org/)
- [MDN WebRTC API](https://developer.mozilla.org/en-US/docs/Web/API/WebRTC_API)
- [STOMP Protocol](https://stomp.github.io/)

## 🎉 구현 완료

음성 채팅 기능이 성공적으로 구현되었습니다!

### 주요 특징

✅ WebRTC P2P 음성 통신  
✅ 실시간 시그널링 (WebSocket/STOMP)  
✅ 클라이언트 기반 음성 변조  
✅ 세션 관리 및 상태 추적  
✅ 완전한 테스트 페이지 제공  
✅ 보안 (JWT 인증)  
✅ 에러 처리 및 로깅  

### 다음 단계

- [ ] 그룹 음성 채팅 지원
- [ ] 음성 녹음 기능
- [ ] 통화 품질 모니터링
- [ ] 통화 내역 저장

