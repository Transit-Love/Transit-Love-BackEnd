# 마지막 선택(Final Choice) 기능 구현 완료

## 📋 개요

매칭 상대를 변경할 수 있는 마지막 선택 기회를 제공하는 기능을 구현했습니다.

## ✨ 주요 기능

### 1. 마지막 선택 옵션 조회
- **현재 매칭 상대**: 현재 매칭된 커플 정보 제공
- **채팅했던 다른 사람들**: 매칭되지 않았지만 채팅 이력이 있는 사람들 목록

### 2. 마지막 선택 실행
- **현재 매칭 유지**: 현재 매칭 상대를 선택하면 그대로 유지
- **매칭 변경**: 채팅했던 다른 사람을 선택하면 매칭 변경
  - 기존 매칭 해제
  - 새로운 매칭 생성
  - 채팅방 타입 변경 (UNMATCHED → MATCHED)

## 🔧 구현 내역

### 1. DTO 생성

#### FinalChoiceRequest.java
```java
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalChoiceRequest {
    private Long selectedProfileId; // 선택한 프로필 ID
}
```

#### FinalChoiceOptionsResponse.java
```java
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalChoiceOptionsResponse {
    private CurrentMatchDto currentMatch;        // 현재 매칭 상대
    private List<ChatPartnerDto> chatPartners;   // 채팅했던 다른 사람들
    
    // 현재 매칭 상대 정보
    public static class CurrentMatchDto {
        private Long matchId;
        private Long profileId;
        private String nickname;
        private String mbti;
        private LocalDateTime matchedAt;
        private Long totalMessageCount;
    }
    
    // 채팅했던 다른 사람 정보
    public static class ChatPartnerDto {
        private Long profileId;
        private String nickname;
        private String mbti;
        private Long totalMessageCount;
        private LocalDateTime lastMessageAt;
    }
}
```

#### FinalChoiceResponse.java
```java
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalChoiceResponse {
    private Long matchId;
    private Long myProfileId;
    private Long partnerProfileId;
    private String partnerNickname;
    private String partnerMbti;
    private Boolean isChanged;              // 매칭이 변경되었는지 여부
    private LocalDateTime finalizedAt;
}
```

### 2. Service 로직 (ChatService.java)

#### getFinalChoiceOptions()
```java
/**
 * 마지막 선택 옵션 조회
 * 현재 매칭 상대 + 채팅했던 다른 사람들
 */
public FinalChoiceOptionsResponse getFinalChoiceOptions(Long profileId)
```

**기능:**
1. 현재 활성 매칭 조회
2. UNMATCHED 채팅방에서 메시지를 주고받은 사람들 조회
3. 현재 매칭 상대는 채팅 파트너 목록에서 제외

#### makeFinalChoice()
```java
/**
 * 마지막 선택 실행
 */
@Transactional
public FinalChoiceResponse makeFinalChoice(Long profileId, Long selectedProfileId)
```

**기능:**
1. 선택한 프로필 검증
2. 현재 활성 매칭 조회
3. 선택 처리:
   - **현재 매칭 상대 선택**: 그대로 유지
   - **다른 사람 선택**: 
     - 채팅 이력 검증
     - 메시지 존재 여부 확인
     - 기존 매칭 해제
     - 선택된 프로필의 기존 매칭도 해제
     - 새로운 매칭 생성
     - ChatRoom을 MATCHED로 변경

### 3. Repository 메서드 추가

#### ChatMessageRepository.java
```java
@Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.chatRoom.id = :chatRoomId")
Long countByChatRoomId(@Param("chatRoomId") Long chatRoomId);
```

### 4. Entity 메서드 추가

#### ChatRoom.java
```java
/**
 * UNMATCHED 채팅방을 MATCHED로 변경
 * 마지막 선택에서 사용
 */
public void convertToMatched(Match match) {
    this.match = match;
    this.roomType = RoomType.MATCHED;
    this.expiresAt = null; // MATCHED가 되면 만료 시간 제거
}
```

### 5. API 엔드포인트 (ChatController.java)

#### GET /api/chat/final-choice/options
마지막 선택 옵션 조회

**Response:**
```json
{
  "currentMatch": {
    "matchId": 1,
    "profileId": 2,
    "nickname": "김철수",
    "mbti": "ENFP",
    "matchedAt": "2024-01-01T12:00:00",
    "totalMessageCount": 50
  },
  "chatPartners": [
    {
      "profileId": 3,
      "nickname": "이영희",
      "mbti": "INFJ",
      "totalMessageCount": 20,
      "lastMessageAt": "2024-01-02T10:00:00"
    }
  ]
}
```

#### POST /api/chat/final-choice
마지막 선택 실행

**Request:**
```json
{
  "selectedProfileId": 3
}
```

**Response:**
```json
{
  "matchId": 5,
  "myProfileId": 1,
  "partnerProfileId": 3,
  "partnerNickname": "이영희",
  "partnerMbti": "INFJ",
  "isChanged": true,
  "finalizedAt": "2024-01-03T15:30:00"
}
```

## 🔍 비즈니스 로직

### 선택 시나리오

#### 시나리오 1: 현재 매칭 상대 유지
1. 사용자가 현재 매칭 상대를 선택
2. 매칭 상태 그대로 유지
3. `isChanged: false` 반환

#### 시나리오 2: 다른 사람 선택
1. 사용자가 채팅했던 다른 프로필 선택
2. 검증:
   - 선택한 프로필과의 채팅방 존재 여부
   - 메시지를 주고받은 이력 확인
3. 매칭 변경:
   - 기존 매칭 해제 (`unmatch()`)
   - 선택된 프로필의 기존 매칭도 해제
   - 새로운 매칭 생성
   - ChatRoom 타입 변경: `UNMATCHED` → `MATCHED`
4. `isChanged: true` 반환

## 📊 데이터베이스 변경

### ChatRoom 상태 변화
- **기존**: `UNMATCHED` (매칭 없이 채팅만)
- **선택 후**: `MATCHED` (정식 매칭)
- **만료 시간**: `MATCHED`로 변경 시 `expiresAt`이 `null`로 설정됨

### Match 상태 변화
- **기존 매칭**: `isActive = false`, `unmatchedAt` 설정
- **새 매칭**: `isActive = true`, `matchType = MANUAL`

## ✅ 검증 로직

1. **프로필 존재 여부**: 선택한 프로필이 존재하는지 확인
2. **활성 매칭 존재**: 사용자에게 활성 매칭이 있는지 확인
3. **채팅 이력 검증**: 선택한 프로필과 채팅방이 존재하는지 확인
4. **메시지 존재 여부**: 최소 1개 이상의 메시지를 주고받았는지 확인

## 🚀 사용 방법

### 1. 마지막 선택 옵션 조회
```bash
curl -X GET http://localhost:8080/api/chat/final-choice/options \
  -H "Authorization: Bearer {token}"
```

### 2. 마지막 선택 실행
```bash
curl -X POST http://localhost:8080/api/chat/final-choice \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "selectedProfileId": 3
  }'
```

## 📝 로그 메시지

```
[INFO] 마지막 선택 옵션 조회 완료 - 현재 매칭: 1, 채팅 파트너: 2
[INFO] 매칭 변경 시작 - 기존: 2, 새로운: 3
[INFO] 기존 매칭 해제 완료 - ID: 1
[INFO] 선택된 프로필의 기존 매칭 해제 - ID: 4
[INFO] 새로운 매칭 생성 완료 - ID: 5
[INFO] 채팅방 타입 변경 (UNMATCHED -> MATCHED) - 채팅방 ID: 10
[INFO] 마지막 선택 완료 - 프로필: 1, 변경 여부: true
```

## 🎯 특징

1. **원자성 보장**: `@Transactional` 적용으로 매칭 변경 시 모든 작업이 원자적으로 처리
2. **양방향 매칭 해제**: 선택된 프로필의 기존 매칭도 함께 해제
3. **채팅방 타입 자동 변환**: UNMATCHED → MATCHED 자동 전환
4. **완전한 검증**: 채팅 이력 및 메시지 존재 여부 검증

## 🔐 보안

- JWT 인증을 통해 본인만 선택 가능
- 채팅 이력이 있는 프로필만 선택 가능
- 최소 1개 이상의 메시지를 주고받은 프로필만 선택 가능

## 📖 추가 참고 사항

- 현재는 1:1 매칭만 지원 (첫 번째 활성 매칭만 사용)
- 마지막 선택은 한 번만 실행 가능한 것으로 설계
- 채팅방의 `expiresAt`이 `null`이 되면 영구적인 매칭으로 전환

## 🎉 구현 완료

모든 기능이 정상적으로 구현되고 빌드가 성공했습니다!

