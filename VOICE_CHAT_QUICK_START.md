# 🚀 음성 채팅 빠른 시작 가이드

## 1️⃣ 서버 실행

```bash
cd /Users/hanjiwon/Desktop/transitLove/BackEnd/transitLove
export JAVA_HOME=/Users/hanjiwon/Library/Java/JavaVirtualMachines/corretto-17.0.16/Contents/Home
./gradlew bootRun
```

## 2️⃣ 테스트 페이지 접속

```
http://localhost:8080/voice-chat-test.html
```

## 3️⃣ 2개의 브라우저 탭 열기

- **탭 1 (발신자)**: 프로필 ID = 1
- **탭 2 (수신자)**: 프로필 ID = 2

## 4️⃣ WebSocket 연결

### 탭 1 (발신자)
1. JWT 토큰 입력
2. 프로필 ID: `1`
3. "연결" 버튼 클릭
4. 상태: "연결됨" 확인

### 탭 2 (수신자)
1. JWT 토큰 입력
2. 프로필 ID: `2`
3. "연결" 버튼 클릭
4. 상태: "연결됨" 확인

## 5️⃣ 음성 통화 시작

### 탭 1 (발신자)
1. 채팅방 ID: `1`
2. 상대방 프로필 ID: `2`
3. "통화 시작" 버튼 클릭
4. 마이크 권한 허용

### 탭 2 (수신자)
1. "통화 요청 받음" 메시지 확인
2. "통화 수락" 버튼 클릭
3. 마이크 권한 허용

## 6️⃣ 통화 확인

- 로그에서 다음 메시지 확인:
  ```
  [시간] 음성 통화 요청
  [시간] 통화 수락
  [시간] WebRTC Offer 전송 완료
  [시간] WebRTC Answer 전송 완료
  [시간] ICE Candidate 추가 완료
  ```

## 7️⃣ 통화 종료

- 아무 탭에서 "통화 종료" 버튼 클릭

## 🎤 음성 변조 테스트

1. "WebRTC 테스트" 버튼 클릭
2. 마이크 권한 허용
3. 로그 확인:
   ```
   마이크 권한 획득 성공
   AudioContext 생성 완료 - 음성 변조 준비됨
   클라이언트에서 음성 변조 처리 가능
   ```

## 📋 체크리스트

- [ ] 서버 실행 완료
- [ ] 2개의 브라우저 탭 열기
- [ ] 각 탭에서 WebSocket 연결 성공
- [ ] 통화 요청 전송
- [ ] 통화 수락
- [ ] WebRTC 연결 확인 (로그)
- [ ] 통화 종료 확인
- [ ] 음성 변조 테스트 완료

## ⚠️ 문제 해결

### WebSocket 연결 실패
- JWT 토큰이 올바른지 확인
- 서버가 실행 중인지 확인
- 브라우저 콘솔에서 에러 확인

### 마이크 권한 거부
- 브라우저 설정에서 마이크 권한 허용
- HTTPS 환경 필요 (localhost는 예외)

### WebRTC 연결 실패
- 방화벽 설정 확인
- STUN 서버 연결 확인
- 네트워크 환경 확인

## 📚 추가 문서

- **상세 API 문서**: `VOICE_CHAT_API_README.md`
- **구현 상세**: `VOICE_CHAT_IMPLEMENTATION.md`

---

**준비 시간**: ~5분  
**테스트 시간**: ~2분  
**총 소요 시간**: ~7분

