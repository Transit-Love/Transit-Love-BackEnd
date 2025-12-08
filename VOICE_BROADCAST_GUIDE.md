# 🔊 음성 통화 브로드캐스팅 가이드

**작성 일자**: 2025-12-09  
**버전**: 1.0.0

---

## 📋 개요

음성 통화 기능의 **브로드캐스팅 시스템**을 구현했습니다. 채팅방의 모든 참여자에게 실시간으로 통화 상태 변경을 알립니다.

---

## 🎯 브로드캐스팅 방식

### 기존 방식 (개별 큐)

❌ **문제점**:
```java
// 각 사용자에게 개별적으로 전송
sendToUser(profileId, message); // /queue/voice.{profileId}
```

- 발신자와 수신자를 구분해서 전송해야 함
- 코드 복잡도 증가
- 확장성 제한

### 새로운 방식 (채팅방 토픽)

✅ **개선점**:
```java
// 채팅방 전체에 브로드캐스트
broadcastToChatRoom(chatRoomId, message); // /topic/voice.{chatRoomId}
```

- 한 번의 전송으로 모든 참여자에게 도달
- 코드 단순화
- 확장 가능 (3인 이상 통화 대비)
- 클라이언트에서 senderId로 필터링

---

## 🔧 구현된 브로드캐스팅 기능

### 1. 기본 통화 시그널 브로드캐스팅

#### 📞 통화 요청

```java
/**
 * 통화 요청 시 채팅방 전체에 브로드캐스트
 */
public void handleVoiceCallRequest(VoiceSignalMessage message) {
    // 세션 생성
    VoiceCallSession session = new VoiceCallSession(...);
    activeSessions.put(chatRoomId, session);
    
    // ✅ 채팅방 전체에 브로드캐스트
    broadcastToChatRoom(chatRoomId, message);
}
```

**브로드캐스트 내용**:
- 발신자 정보
- 수신자 정보
- 통화 요청 시각

#### ✅ 통화 수락

```java
/**
 * 통화 수락 시 채팅방 전체에 브로드캐스트
 */
public void handleVoiceCallAccept(VoiceSignalMessage message) {
    session.setAccepted(true);
    
    // ✅ 채팅방 전체에 브로드캐스트
    broadcastToChatRoom(chatRoomId, message);
}
```

**브로드캐스트 내용**:
- 수락자 정보
- 통화 시작 시각

#### ❌ 통화 거절

```java
/**
 * 통화 거절 시 채팅방 전체에 브로드캐스트
 */
public void handleVoiceCallReject(VoiceSignalMessage message) {
    activeSessions.remove(chatRoomId);
    
    // ✅ 채팅방 전체에 브로드캐스트
    broadcastToChatRoom(chatRoomId, message);
}
```

**브로드캐스트 내용**:
- 거절자 정보
- 거절 사유 (선택)

#### 📴 통화 종료

```java
/**
 * 통화 종료 시 채팅방 전체에 브로드캐스트
 */
public void handleVoiceCallEnd(VoiceSignalMessage message) {
    activeSessions.remove(chatRoomId);
    
    // ✅ 채팅방 전체에 브로드캐스트
    broadcastToChatRoom(chatRoomId, message);
}
```

**브로드캐스트 내용**:
- 종료자 정보
- 통화 종료 시각

#### 🚫 통화 취소

```java
/**
 * 통화 취소 시 채팅방 전체에 브로드캐스트
 */
public void handleVoiceCallCancel(VoiceSignalMessage message) {
    activeSessions.remove(chatRoomId);
    
    // ✅ 채팅방 전체에 브로드캐스트
    broadcastToChatRoom(chatRoomId, message);
}
```

**브로드캐스트 내용**:
- 취소자 정보 (발신자)
- 취소 시각

---

### 2. WebRTC 시그널 브로드캐스팅

#### 🔄 Offer/Answer 교환

```java
/**
 * WebRTC Offer/Answer 브로드캐스트
 */
public void handleOffer(VoiceSignalMessage message) {
    broadcastToChatRoom(message.getChatRoomId(), message);
}

public void handleAnswer(VoiceSignalMessage message) {
    broadcastToChatRoom(message.getChatRoomId(), message);
}
```

**브로드캐스트 내용**:
- SDP (Session Description Protocol) 정보
- 발신자/수신자 정보

#### 🧊 ICE Candidate 교환

```java
/**
 * ICE Candidate 브로드캐스트
 */
public void handleIceCandidate(VoiceSignalMessage message) {
    broadcastToChatRoom(message.getChatRoomId(), message);
}
```

**브로드캐스트 내용**:
- ICE Candidate 정보
- 네트워크 경로 정보

---

### 3. 추가 브로드캐스팅 기능

#### 🔇 통화 중 상태 변경

```java
/**
 * 음소거, 스피커 상태 등의 변경을 채팅방 전체에 알림
 */
public void broadcastStatusChange(VoiceSignalMessage message) {
    broadcastToChatRoom(message.getChatRoomId(), message);
    log.debug("통화 상태 변경 브로드캐스트 - 채팅방: {}, 타입: {}", 
            message.getChatRoomId(), message.getType());
}
```

**사용 예시**:
```java
// 음소거 상태 변경
VoiceSignalMessage muteMessage = VoiceSignalMessage.builder()
    .type(VoiceSignalType.MUTE_STATUS_CHANGED)
    .chatRoomId(chatRoomId)
    .fromProfileId(myProfileId)
    .isMuted(true)
    .build();
voiceChatService.broadcastStatusChange(muteMessage);

// 스피커 상태 변경
VoiceSignalMessage speakerMessage = VoiceSignalMessage.builder()
    .type(VoiceSignalType.SPEAKER_STATUS_CHANGED)
    .chatRoomId(chatRoomId)
    .fromProfileId(myProfileId)
    .isSpeakerOn(false)
    .build();
voiceChatService.broadcastStatusChange(speakerMessage);
```

#### 🔗 통화 연결 성공 알림

```java
/**
 * WebRTC 연결이 완료되면 호출
 */
public void broadcastCallConnected(Long chatRoomId, Long callerProfileId, Long receiverProfileId) {
    VoiceCallSession session = activeSessions.get(chatRoomId);
    if (session != null && session.isAccepted()) {
        VoiceSignalMessage message = VoiceSignalMessage.builder()
                .type(VoiceSignalType.VOICE_CALL_ACCEPT)
                .chatRoomId(chatRoomId)
                .fromProfileId(receiverProfileId)
                .toProfileId(callerProfileId)
                .senderId(receiverProfileId)
                .timestamp(System.currentTimeMillis())
                .build();

        broadcastToChatRoom(chatRoomId, message);
        log.info("통화 연결 성공 브로드캐스트 - 채팅방: {}", chatRoomId);
    }
}
```

**사용 예시**:
```java
// WebRTC PeerConnection 연결 성공 후
peerConnection.onConnectionStateChange = (state) -> {
    if (state == "connected") {
        voiceChatService.broadcastCallConnected(chatRoomId, callerId, receiverId);
    }
};
```

#### 🔄 통화 상태 동기화

```java
/**
 * 클라이언트가 재연결 시 현재 통화 상태를 확인하고 동기화
 */
public void syncCallStatus(Long chatRoomId, Long profileId) {
    VoiceCallSession session = activeSessions.get(chatRoomId);
    if (session != null) {
        VoiceSignalMessage statusMessage = VoiceSignalMessage.builder()
                .type(VoiceSignalType.VOICE_CALL_REQUEST)
                .chatRoomId(chatRoomId)
                .fromProfileId(session.getCallerId())
                .toProfileId(session.getReceiverId())
                .senderId(0L) // 시스템 메시지
                .timestamp(System.currentTimeMillis())
                .build();

        // 개별 사용자에게만 전송
        sendToUser(profileId, statusMessage);
        log.info("통화 상태 동기화 - 채팅방: {}, 프로필: {}", chatRoomId, profileId);
    }
}
```

**사용 예시**:
```java
// WebSocket 재연결 시
@EventListener
public void onWebSocketConnected(SessionConnectedEvent event) {
    Long profileId = getProfileIdFromSession(event);
    Long chatRoomId = getCurrentChatRoom(profileId);
    
    // 진행 중인 통화가 있으면 동기화
    voiceChatService.syncCallStatus(chatRoomId, profileId);
}
```

#### 📢 일반 알림 브로드캐스트

```java
/**
 * 통화 관련 일반 알림용 (연결 품질, 네트워크 상태 등)
 */
public void broadcastNotification(Long chatRoomId, String notificationType, String message) {
    VoiceSignalMessage notification = VoiceSignalMessage.builder()
            .type(VoiceSignalType.ERROR)
            .chatRoomId(chatRoomId)
            .errorMessage(notificationType + ": " + message)
            .timestamp(System.currentTimeMillis())
            .build();

    broadcastToChatRoom(chatRoomId, notification);
    log.info("일반 알림 브로드캐스트 - 채팅방: {}, 타입: {}, 메시지: {}", 
            chatRoomId, notificationType, message);
}
```

**사용 예시**:
```java
// 네트워크 품질 저하
voiceChatService.broadcastNotification(
    chatRoomId, 
    "NETWORK_QUALITY", 
    "연결 품질이 낮습니다. 통화 품질이 저하될 수 있습니다."
);

// 배터리 부족
voiceChatService.broadcastNotification(
    chatRoomId, 
    "LOW_BATTERY", 
    "배터리가 부족합니다. 통화가 종료될 수 있습니다."
);
```

---

### 4. 유틸리티 메서드

#### 🔍 활성 세션 조회

```java
/**
 * 모든 활성 세션 조회
 */
public Map<Long, VoiceCallSession> getAllActiveSessions() {
    return new ConcurrentHashMap<>(activeSessions);
}

/**
 * 특정 프로필의 활성 통화 조회
 */
public VoiceCallSession getActiveSessionByProfile(Long profileId) {
    return activeSessions.values().stream()
            .filter(session -> 
                    session.getCallerId().equals(profileId) || 
                    session.getReceiverId().equals(profileId))
            .findFirst()
            .orElse(null);
}
```

**사용 예시**:
```java
// 사용자가 현재 통화 중인지 확인
VoiceCallSession session = voiceChatService.getActiveSessionByProfile(profileId);
if (session != null) {
    log.info("사용자 {}는 채팅방 {}에서 통화 중", profileId, session.getChatRoomId());
}
```

---

## 🌐 프론트엔드 통합

### WebSocket 구독

```typescript
// 채팅방 토픽 구독
stompClient.subscribe(`/topic/voice.${chatRoomId}`, (message) => {
  const signal = JSON.parse(message.body);
  
  // ⭐ 자신이 보낸 메시지 필터링
  if (signal.senderId !== myProfileId) {
    handleIncomingSignal(signal);
  }
});
```

### 시그널 처리

```typescript
function handleIncomingSignal(signal: VoiceSignalMessage) {
  switch (signal.type) {
    case 'VOICE_CALL_REQUEST':
      // 📞 통화 요청 받음
      showIncomingCallUI(signal.fromProfileId);
      playRingtone();
      break;
      
    case 'VOICE_CALL_ACCEPT':
      // ✅ 통화 수락됨
      stopRingtone();
      startWebRTCConnection();
      break;
      
    case 'VOICE_CALL_REJECT':
      // ❌ 통화 거절됨
      stopRingtone();
      showRejectedNotification();
      break;
      
    case 'VOICE_CALL_END':
      // 📴 통화 종료됨
      endCall();
      showCallEndedNotification();
      break;
      
    case 'VOICE_CALL_CANCEL':
      // 🚫 통화 취소됨
      stopRingtone();
      showCallCancelledNotification();
      break;
      
    case 'MUTE_STATUS_CHANGED':
      // 🔇 상대방 음소거 상태 변경
      updatePartnerMuteStatus(signal.isMuted);
      break;
      
    case 'SPEAKER_STATUS_CHANGED':
      // 🔊 상대방 스피커 상태 변경
      updatePartnerSpeakerStatus(signal.isSpeakerOn);
      break;
      
    case 'OFFER':
      // 🔄 WebRTC Offer 받음
      handleOffer(signal.sdp);
      break;
      
    case 'ANSWER':
      // 🔄 WebRTC Answer 받음
      handleAnswer(signal.sdp);
      break;
      
    case 'ICE_CANDIDATE':
      // 🧊 ICE Candidate 받음
      handleIceCandidate(signal.iceCandidate);
      break;
      
    case 'ERROR':
      // ⚠️ 에러 또는 알림
      showNotification(signal.errorMessage);
      break;
  }
}
```

---

## 📊 브로드캐스팅 흐름도

### 통화 요청 → 수락 → 종료

```
발신자 (Profile 1)          서버 (VoiceChatService)          수신자 (Profile 2)
      |                              |                              |
      |  VOICE_CALL_REQUEST          |                              |
      |----------------------------->|                              |
      |                              |  브로드캐스트                  |
      |                              |  /topic/voice.{chatRoomId}   |
      |                              |----------------------------->|
      |                              |                              |📞 전화옴
      |                              |                              |
      |                              |  VOICE_CALL_ACCEPT           |
      |                              |<-----------------------------|
      |                              |  브로드캐스트                  |
      |  ✅ 수락됨                    |  /topic/voice.{chatRoomId}   |
      |<-----------------------------|                              |
      |                              |                              |
      |  OFFER (SDP)                 |                              |
      |----------------------------->|----------------------------->|
      |                              |  ANSWER (SDP)                |
      |<-----------------------------|<-----------------------------|
      |                              |                              |
      |  ICE_CANDIDATE ↔             |             ↔ ICE_CANDIDATE  |
      |<-----------------------------|----------------------------->|
      |                              |                              |
      |  🔗 통화 연결 성공             |                              |
      |                              |                              |
      |  MUTE_STATUS_CHANGED         |                              |
      |----------------------------->|----------------------------->|
      |                              |                              |🔇 음소거
      |                              |                              |
      |  VOICE_CALL_END              |                              |
      |----------------------------->|----------------------------->|
      |                              |                              |📴 종료
```

---

## ✅ 브로드캐스팅의 장점

### 1. 코드 단순화
- 발신자/수신자 구분 불필요
- 한 번의 전송으로 모든 참여자에게 도달

### 2. 확장성
- 3인 이상 통화 지원 가능
- 그룹 통화 대비

### 3. 일관성
- 모든 참여자가 동일한 상태 유지
- 동기화 문제 최소화

### 4. 유연성
- 새로운 시그널 타입 추가 용이
- 클라이언트에서 필터링 가능

---

## 🧪 테스트 방법

### 1. 브라우저 테스트 페이지

```
http://localhost:8080/voice-signaling-test.html
```

1. 두 개의 브라우저 탭 열기
2. 각각 다른 프로필로 로그인
3. 같은 채팅방 구독
4. 한쪽에서 통화 요청
5. 다른 쪽에서 메시지 수신 확인

### 2. 로그 확인

```
음성 통화 요청 브로드캐스트 - 채팅방: 1, 발신자: 1, 수신자: 2
메시지 브로드캐스트 - 목적지: /topic/voice.1, 타입: VOICE_CALL_REQUEST
```

---

## 📚 관련 문서

- **VOICE_CHAT_IMPLEMENTATION_FINAL.md** - 전체 구현 보고서
- **VOICE_CHAT_QUICK_START.md** - 빠른 시작 가이드
- **VOICE_SIGNALING_BUGFIX.md** - 버그 수정 보고서

---

**작성 일자**: 2025-12-09  
**버전**: 1.0.0  
**상태**: ✅ 구현 완료

