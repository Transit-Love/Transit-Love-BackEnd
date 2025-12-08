# 프로필 목록 조회 API 가이드

## 개요
매칭된 프로필과 매칭되지 않은 프로필(매칭 가능한 후보)을 조회할 수 있는 API입니다.

---

## API 목록

### 1. 매칭된 프로필 목록 조회 (나와 매칭된 상대방들)

**Endpoint**: `GET /api/profiles/matched`

**설명**: 현재 로그인한 사용자와 매칭된 프로필들의 목록을 조회합니다.

**인증**: 필요 (Bearer Token)

#### Request
```http
GET /api/profiles/matched HTTP/1.1
Host: localhost:8080
Authorization: Bearer {access_token}
```

#### Response (Success - 200 OK)
```json
[
  {
    "id": 2,
    "nickname": "사랑이",
    "mbti": "ENFP",
    "keywords": [
      {
        "id": 1,
        "name": "영화감상"
      },
      {
        "id": 5,
        "name": "운동"
      }
    ],
    "balanceGameAnswers": [
      {
        "balanceGameId": 1,
        "question": "데이트할 때 더 좋은 것은?",
        "option1": "카페",
        "option2": "산책",
        "selectedOption": 1
      }
    ],
    "createdAt": "2024-12-08T10:30:00"
  },
  {
    "id": 3,
    "nickname": "행복이",
    "mbti": "ISFJ",
    "keywords": [
      {
        "id": 2,
        "name": "독서"
      },
      {
        "id": 3,
        "name": "요리"
      }
    ],
    "balanceGameAnswers": [
      {
        "balanceGameId": 1,
        "question": "데이트할 때 더 좋은 것은?",
        "option1": "카페",
        "option2": "산책",
        "selectedOption": 2
      }
    ],
    "createdAt": "2024-12-08T11:00:00"
  }
]
```

---

### 2. 매칭되지 않은 프로필 목록 조회 (매칭 가능한 후보들)

**Endpoint**: `GET /api/profiles/unmatched`

**설명**: 아직 누구와도 매칭되지 않은 프로필들의 목록을 조회합니다. 자기 자신과 이미 매칭된 사람들은 제외됩니다.

**인증**: 필요 (Bearer Token)

#### Request
```http
GET /api/profiles/unmatched HTTP/1.1
Host: localhost:8080
Authorization: Bearer {access_token}
```

#### Response (Success - 200 OK)
```json
[
  {
    "id": 5,
    "nickname": "자유로운영혼",
    "mbti": "ENTP",
    "keywords": [
      {
        "id": 4,
        "name": "여행"
      },
      {
        "id": 6,
        "name": "사진촬영"
      }
    ],
    "balanceGameAnswers": [
      {
        "balanceGameId": 1,
        "question": "데이트할 때 더 좋은 것은?",
        "option1": "카페",
        "option2": "산책",
        "selectedOption": 2
      },
      {
        "balanceGameId": 2,
        "question": "주말에 더 하고 싶은 것은?",
        "option1": "집에서 쉬기",
        "option2": "밖에 나가기",
        "selectedOption": 2
      }
    ],
    "createdAt": "2024-12-08T12:00:00"
  },
  {
    "id": 6,
    "nickname": "평화주의자",
    "mbti": "INFP",
    "keywords": [
      {
        "id": 2,
        "name": "독서"
      },
      {
        "id": 7,
        "name": "음악감상"
      }
    ],
    "balanceGameAnswers": [
      {
        "balanceGameId": 1,
        "question": "데이트할 때 더 좋은 것은?",
        "option1": "카페",
        "option2": "산책",
        "selectedOption": 1
      }
    ],
    "createdAt": "2024-12-08T13:00:00"
  }
]
```

---

## Response 필드 설명

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | Long | 프로필 ID |
| `nickname` | String | 닉네임 |
| `mbti` | String | MBTI 유형 (4자리) |
| `keywords` | Array | 관심사 키워드 목록 |
| `keywords[].id` | Long | 키워드 ID |
| `keywords[].name` | String | 키워드 이름 |
| `balanceGameAnswers` | Array | 밸런스 게임 답변 목록 |
| `balanceGameAnswers[].balanceGameId` | Long | 밸런스 게임 ID |
| `balanceGameAnswers[].question` | String | 질문 내용 |
| `balanceGameAnswers[].option1` | String | 선택지 1 |
| `balanceGameAnswers[].option2` | String | 선택지 2 |
| `balanceGameAnswers[].selectedOption` | Integer | 선택한 옵션 (1 또는 2) |
| `createdAt` | DateTime | 프로필 생성 시간 |

---

## 에러 응답

### 401 Unauthorized
인증 토큰이 없거나 유효하지 않은 경우
```json
{
  "error": "Unauthorized",
  "message": "인증이 필요합니다."
}
```

### 404 Not Found
사용자의 프로필을 찾을 수 없는 경우
```json
{
  "error": "Not Found",
  "message": "프로필을 찾을 수 없습니다."
}
```

---

## 사용 예시

### cURL을 이용한 매칭된 프로필 조회
```bash
curl -X GET "http://localhost:8080/api/profiles/matched" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### cURL을 이용한 매칭되지 않은 프로필 조회
```bash
curl -X GET "http://localhost:8080/api/profiles/unmatched" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### JavaScript (Fetch API)를 이용한 매칭된 프로필 조회
```javascript
fetch('http://localhost:8080/api/profiles/matched', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => {
  console.log('매칭된 프로필:', data);
})
.catch(error => {
  console.error('에러 발생:', error);
});
```

### JavaScript (Fetch API)를 이용한 매칭되지 않은 프로필 조회
```javascript
fetch('http://localhost:8080/api/profiles/unmatched', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => {
  console.log('매칭 가능한 프로필:', data);
})
.catch(error => {
  console.error('에러 발생:', error);
});
```

---

## 비즈니스 로직 설명

### 매칭된 프로필 조회 (`/api/profiles/matched`)
1. 현재 로그인한 사용자의 프로필을 조회합니다.
2. 해당 프로필의 활성화된(isActive = true) 매칭 목록을 조회합니다.
3. 각 매칭에서 상대방 프로필을 추출합니다.
   - 내가 profile1이면 profile2를 반환
   - 내가 profile2면 profile1을 반환
4. 상대방 프로필들을 `ProfileResponse` 형태로 변환하여 반환합니다.

### 매칭되지 않은 프로필 조회 (`/api/profiles/unmatched`)
1. 전체 활성화된 매칭 목록에서 이미 매칭된 프로필 ID들을 수집합니다.
2. 전체 프로필 중에서 다음 조건을 만족하는 프로필들을 필터링합니다:
   - 자기 자신이 아닌 프로필
   - 아직 누구와도 매칭되지 않은 프로필 (매칭된 프로필 ID 목록에 없는 프로필)
3. 필터링된 프로필들을 `ProfileResponse` 형태로 변환하여 반환합니다.

---

## 활용 예시

### 1. 매칭된 상대방 목록 화면
- `/api/profiles/matched` API를 사용하여 내가 매칭된 사람들의 프로필 정보를 표시
- 각 프로필을 클릭하면 채팅방으로 이동 가능

### 2. 매칭 추천 화면
- `/api/profiles/unmatched` API를 사용하여 매칭 가능한 후보들을 표시
- 각 프로필의 MBTI, 관심사, 밸런스 게임 답변을 기반으로 유사도를 계산하여 정렬 가능
- 마음에 드는 프로필에 대해 매칭 요청 기능 제공 가능

### 3. 통계 화면
- 매칭된 프로필 수 vs 매칭 가능한 프로필 수 비교
- 전체 프로필 현황 파악

---

## 참고사항

1. **인증**: 두 API 모두 인증이 필요하며, OAuth2 로그인 후 받은 JWT 토큰을 사용해야 합니다.

2. **테스트 모드**: 현재 코드는 테스트용으로 OAuth2User가 null일 경우 userId 1을 사용하도록 되어 있습니다.

3. **성능**: 프로필 수가 많아질 경우, 페이징 기능을 추가하는 것을 권장합니다.

4. **필터링**: 현재는 단순히 매칭 여부로만 필터링하지만, 향후 성별, 나이, 지역 등의 추가 필터링 조건을 적용할 수 있습니다.

5. **정렬**: 매칭되지 않은 프로필 목록은 `/api/matching/recommendations` API와 함께 사용하면 유사도 기반으로 정렬된 추천 목록을 받을 수 있습니다.

---

## 구현 상세

### Service Layer (ProfileService.java)
```java
/**
 * 매칭된 프로필 목록 조회 (내가 매칭된 상대방들)
 */
public List<ProfileResponse> getMatchedProfiles(Long userId)

/**
 * 매칭되지 않은 프로필 목록 조회 (매칭 가능한 후보들)
 */
public List<ProfileResponse> getUnmatchedProfiles(Long userId)
```

### Controller Layer (ProfileController.java)
```java
/**
 * GET /api/profiles/matched
 */
@GetMapping("/matched")
public ResponseEntity<List<ProfileResponse>> getMatchedProfiles(@AuthenticationPrincipal CustomOAuth2User oAuth2User)

/**
 * GET /api/profiles/unmatched
 */
@GetMapping("/unmatched")
public ResponseEntity<List<ProfileResponse>> getUnmatchedProfiles(@AuthenticationPrincipal CustomOAuth2User oAuth2User)
```

---

## 관련 API

- **[GET /api/matching/recommendations](./MATCHING_API_README.md)** - 유사도 기반 매칭 추천 목록 (정렬됨)
- **[POST /api/matching/auto-match](./MATCHING_API_README.md)** - 자동 매칭 수행
- **[GET /api/profiles/me](./PROFILE_API_README.md)** - 내 프로필 조회
- **[GET /api/chat](./CHAT_LIST_API_README.md)** - 채팅방 목록 조회 (매칭된 사람과의 채팅방)

