# 마지막 선택(Final Choice) 기능 - README

## 🎯 기능 개요

사용자가 현재 매칭 상대를 유지하거나, 채팅했던 다른 사람으로 매칭을 변경할 수 있는 **마지막 선택** 기능입니다.

## 💡 사용자 시나리오

```
1. 사용자는 자동 매칭으로 프로필B와 매칭됨
2. 매칭 후 프로필C, D와도 자유롭게 채팅 가능 (UNMATCHED 채팅)
3. 일정 시간 후 "마지막 선택" 페이지 제공
4. 선택지:
   ✅ 프로필B (현재 매칭) 선택 → 그대로 유지
   ✅ 프로필C 선택 → 매칭 변경 (B→C)
   ✅ 프로필D 선택 → 매칭 변경 (B→D)
```

## 📌 주요 특징

### 1. **한 번의 기회**
- 마지막 선택은 딱 한 번만 제공
- 신중한 선택 유도

### 2. **채팅 이력 기반**
- 메시지를 주고받은 사람만 선택 가능
- 최소 1개 이상의 메시지 필요

### 3. **원자적 매칭 변경**
- 기존 매칭 해제
- 새 매칭 생성
- 채팅방 타입 자동 변환
- 모든 작업이 트랜잭션으로 처리

### 4. **양방향 매칭 처리**
- 선택된 사람의 기존 매칭도 자동 해제
- 충돌 방지

## 🔧 API 엔드포인트

### 1. 선택 옵션 조회
```http
GET /api/chat/final-choice/options
Authorization: Bearer {token}
```

**응답**: 현재 매칭 상대 + 채팅했던 사람들 목록

---

### 2. 선택 실행
```http
POST /api/chat/final-choice
Authorization: Bearer {token}
Content-Type: application/json

{
  "selectedProfileId": 3
}
```

**응답**: 최종 매칭 정보 + 변경 여부

## 📂 구현 파일

### DTO
- `FinalChoiceRequest.java` - 선택 요청
- `FinalChoiceResponse.java` - 선택 응답
- `FinalChoiceOptionsResponse.java` - 옵션 조회 응답

### Service
- `ChatService.java` - 비즈니스 로직
  - `getFinalChoiceOptions()` - 옵션 조회
  - `makeFinalChoice()` - 선택 실행

### Controller
- `ChatController.java` - API 엔드포인트
  - `GET /api/chat/final-choice/options`
  - `POST /api/chat/final-choice`

### Entity
- `ChatRoom.java` - `convertToMatched()` 메서드 추가

### Repository
- `ChatMessageRepository.java` - `countByChatRoomId()` 추가

## 🚀 빠른 시작

### 1. 서버 실행
```bash
./start-server.sh
```

### 2. 테스트 스크립트 실행
```bash
./test-final-choice-api.sh
```

### 3. 또는 Postman 사용
자세한 내용은 [FINAL_CHOICE_POSTMAN_GUIDE.md](./FINAL_CHOICE_POSTMAN_GUIDE.md) 참조

## 📖 문서

- **구현 상세**: [FINAL_CHOICE_IMPLEMENTATION.md](./FINAL_CHOICE_IMPLEMENTATION.md)
- **Postman 가이드**: [FINAL_CHOICE_POSTMAN_GUIDE.md](./FINAL_CHOICE_POSTMAN_GUIDE.md)
- **테스트 스크립트**: [test-final-choice-api.sh](./test-final-choice-api.sh)

## 🎨 프론트엔드 연동 예시

### React 예시

```javascript
// 1. 옵션 조회
const fetchFinalChoiceOptions = async () => {
  const response = await fetch('/api/chat/final-choice/options', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  const data = await response.json();
  
  setCurrentMatch(data.currentMatch);
  setChatPartners(data.chatPartners);
};

// 2. 선택 실행
const makeFinalChoice = async (selectedProfileId) => {
  const response = await fetch('/api/chat/final-choice', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ selectedProfileId })
  });
  const result = await response.json();
  
  if (result.isChanged) {
    alert(`매칭이 변경되었습니다! 새 파트너: ${result.partnerNickname}`);
  } else {
    alert(`현재 매칭을 유지합니다.`);
  }
};
```

### UI 구성 예시

```jsx
<div className="final-choice-page">
  <h1>마지막 선택</h1>
  
  {/* 현재 매칭 */}
  {currentMatch && (
    <div className="current-match">
      <h2>현재 매칭 상대</h2>
      <ProfileCard profile={currentMatch} />
      <button onClick={() => makeFinalChoice(currentMatch.profileId)}>
        이 사람과 계속하기
      </button>
    </div>
  )}
  
  {/* 다른 선택지 */}
  {chatPartners.length > 0 && (
    <div className="other-options">
      <h2>다른 선택지</h2>
      {chatPartners.map(partner => (
        <div key={partner.profileId}>
          <ProfileCard profile={partner} />
          <button onClick={() => makeFinalChoice(partner.profileId)}>
            이 사람으로 변경하기
          </button>
        </div>
      ))}
    </div>
  )}
</div>
```

## ⚠️ 주의사항

1. **되돌릴 수 없음**: 선택 후 변경 불가
2. **채팅 이력 필수**: 메시지를 주고받지 않은 사람은 선택 불가
3. **활성 매칭 필요**: 현재 활성 매칭이 있어야 선택 가능

## 🔒 보안

- JWT 인증 필수
- 본인의 선택만 가능
- 채팅 이력이 있는 프로필만 선택 가능

## 📊 데이터베이스 영향

### 매칭 변경 시

1. **Match 테이블**
   - 기존 매칭: `isActive = false`, `unmatchedAt` 설정
   - 새 매칭: `isActive = true`, `matchType = MANUAL` 생성

2. **ChatRoom 테이블**
   - 선택된 채팅방: `roomType = MATCHED`, `match_id` 설정
   - 만료 시간: `expiresAt = null`

## 🎉 완료

모든 기능이 구현되고 테스트 준비가 완료되었습니다!

---

**작성일**: 2024-01-03  
**버전**: 1.0.0  
**작성자**: TransitLove Team

