# 내 프로필 조회 API 가이드

## 개요
현재 로그인한 사용자의 프로필 정보(닉네임, MBTI, 키워드, 밸런스 게임 답변 등)를 조회하는 API입니다.

---

## API 정보

### 내 프로필 조회

**Endpoint**: `GET /api/profiles/me`

**설명**: 현재 로그인한 사용자의 프로필 정보를 조회합니다.

**인증**: 필요 (Bearer Token)

#### Request
```http
GET /api/profiles/me HTTP/1.1
Host: localhost:8080
Authorization: Bearer {access_token}
```

#### Response (Success - 200 OK)
```json
{
  "id": 1,
  "nickname": "행복한사람",
  "mbti": "ENFP",
  "keywords": [
    {
      "id": 1,
      "name": "영화감상"
    },
    {
      "id": 5,
      "name": "운동"
    },
    {
      "id": 8,
      "name": "게임"
    }
  ],
  "balanceGameAnswers": [
    {
      "balanceGameId": 1,
      "question": "데이트할 때 더 좋은 것은?",
      "option1": "카페",
      "option2": "산책",
      "selectedOption": 1
    },
    {
      "balanceGameId": 2,
      "question": "주말에 더 하고 싶은 것은?",
      "option1": "집에서 쉬기",
      "option2": "밖에 나가기",
      "selectedOption": 2
    }
  ],
  "createdAt": "2024-12-08T10:30:00"
}
```

---

## Response 필드 설명

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | Long | 프로필 ID |
| `nickname` | String | 닉네임 |
| `mbti` | String | MBTI 유형 (4자리, 예: ENFP, ISTJ) |
| `keywords` | Array | 관심사 키워드 목록 |
| `keywords[].id` | Long | 키워드 ID |
| `keywords[].name` | String | 키워드 이름 |
| `balanceGameAnswers` | Array | 밸런스 게임 답변 목록 |
| `balanceGameAnswers[].balanceGameId` | Long | 밸런스 게임 ID |
| `balanceGameAnswers[].question` | String | 질문 내용 |
| `balanceGameAnswers[].option1` | String | 선택지 1 |
| `balanceGameAnswers[].option2` | String | 선택지 2 |
| `balanceGameAnswers[].selectedOption` | Integer | 선택한 옵션 (1 또는 2) |
| `createdAt` | DateTime | 프로필 생성 시간 (ISO 8601 형식) |

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
사용자의 프로필이 존재하지 않는 경우
```json
{
  "error": "Not Found",
  "message": "프로필을 찾을 수 없습니다."
}
```

---

## 사용 예시

### cURL
```bash
curl -X GET "http://localhost:8080/api/profiles/me" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### JavaScript (Fetch API)
```javascript
fetch('http://localhost:8080/api/profiles/me', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  }
})
.then(response => {
  if (!response.ok) {
    throw new Error('프로필 조회 실패');
  }
  return response.json();
})
.then(data => {
  console.log('내 프로필:', data);
  console.log('닉네임:', data.nickname);
  console.log('MBTI:', data.mbti);
  console.log('키워드:', data.keywords.map(k => k.name).join(', '));
})
.catch(error => {
  console.error('에러 발생:', error);
});
```

### JavaScript (Axios)
```javascript
axios.get('http://localhost:8080/api/profiles/me', {
  headers: {
    'Authorization': `Bearer ${accessToken}`
  }
})
.then(response => {
  const profile = response.data;
  console.log('내 프로필:', profile);
})
.catch(error => {
  console.error('에러 발생:', error.response?.data || error.message);
});
```

### React 컴포넌트 예시
```javascript
import React, { useEffect, useState } from 'react';

function MyProfile() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  useEffect(() => {
    const fetchMyProfile = async () => {
      try {
        const token = localStorage.getItem('accessToken');
        const response = await fetch('http://localhost:8080/api/profiles/me', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        
        if (!response.ok) {
          throw new Error('프로필 조회 실패');
        }
        
        const data = await response.json();
        setProfile(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };
    
    fetchMyProfile();
  }, []);
  
  if (loading) return <div>로딩 중...</div>;
  if (error) return <div>에러: {error}</div>;
  if (!profile) return <div>프로필이 없습니다.</div>;
  
  return (
    <div className="profile-card">
      <h2>{profile.nickname}</h2>
      <p>MBTI: {profile.mbti}</p>
      
      <div className="keywords">
        <h3>관심사</h3>
        <ul>
          {profile.keywords.map(keyword => (
            <li key={keyword.id}>{keyword.name}</li>
          ))}
        </ul>
      </div>
      
      <div className="balance-games">
        <h3>밸런스 게임 답변</h3>
        {profile.balanceGameAnswers.map(answer => (
          <div key={answer.balanceGameId}>
            <p><strong>Q: {answer.question}</strong></p>
            <p>선택: {answer.selectedOption === 1 ? answer.option1 : answer.option2}</p>
          </div>
        ))}
      </div>
      
      <p className="created-at">
        가입일: {new Date(profile.createdAt).toLocaleDateString('ko-KR')}
      </p>
    </div>
  );
}

export default MyProfile;
```

---

## 활용 시나리오

### 1. 프로필 화면
사용자가 자신의 프로필 정보를 확인하는 화면에서 사용:
- 닉네임 표시
- MBTI 표시
- 선택한 관심사 키워드 표시
- 밸런스 게임 답변 표시

### 2. 프로필 수정 화면
프로필 수정 전 기존 정보를 불러올 때 사용:
- GET `/api/profiles/me`로 현재 정보 조회
- 폼에 기존 정보 채우기
- PUT `/api/profiles/me`로 수정 요청

### 3. 네비게이션 바
상단 네비게이션에 사용자 정보를 표시할 때:
- 닉네임 표시
- MBTI 뱃지 표시

### 4. 매칭 설정 확인
매칭 전 내 프로필이 완성되었는지 확인:
- 닉네임 설정 여부
- MBTI 입력 여부
- 키워드 선택 여부
- 밸런스 게임 답변 여부

---

## 주의사항

1. **인증 필수**: 반드시 OAuth2 로그인 후 받은 JWT 토큰을 사용해야 합니다.

2. **프로필 미생성 시**: 프로필이 아직 생성되지 않았다면 404 에러가 발생합니다. 이 경우 프로필 생성 화면으로 안내해야 합니다.

3. **토큰 만료**: JWT 토큰이 만료되면 401 에러가 발생합니다. 이 경우 재로그인이 필요합니다.

4. **데이터 캐싱**: 프로필 정보는 자주 변경되지 않으므로 적절한 캐싱 전략을 사용하면 성능을 개선할 수 있습니다.

---

## 관련 API

- **[POST /api/profiles](./PROFILE_API_README.md)** - 프로필 생성
- **[PUT /api/profiles/me](./PROFILE_API_README.md)** - 프로필 수정
- **[GET /api/profiles/matched](./PROFILE_LIST_API_README.md)** - 매칭된 프로필 목록 조회
- **[GET /api/profiles/unmatched](./PROFILE_LIST_API_README.md)** - 매칭 가능한 프로필 목록 조회

---

## 구현 정보

### Controller
```java
// ProfileController.java
@GetMapping("/me")
public ResponseEntity<ProfileResponse> getMyProfile(
    @AuthenticationPrincipal CustomOAuth2User oAuth2User
) {
    Long userId = (oAuth2User != null) ? oAuth2User.getId() : 1L;
    ProfileResponse response = profileService.getProfile(userId);
    return ResponseEntity.ok(response);
}
```

### Service
```java
// ProfileService.java
public ProfileResponse getProfile(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    
    Profile profile = profileRepository.findByUser(user)
        .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));
    
    return ProfileResponse.from(profile);
}
```

### Response DTO
```java
// ProfileResponse.java
@Getter
@Builder
public class ProfileResponse {
    private Long id;
    private String nickname;
    private String mbti;
    private List<KeywordDto> keywords;
    private List<BalanceGameAnswerDto> balanceGameAnswers;
    private LocalDateTime createdAt;
    
    public static ProfileResponse from(Profile profile) {
        // Profile 엔티티를 DTO로 변환
    }
}
```

---

## 테스트

### Postman 테스트
1. OAuth2 로그인으로 JWT 토큰 받기
2. 새 요청 생성:
   - Method: GET
   - URL: `http://localhost:8080/api/profiles/me`
   - Headers:
     - Authorization: `Bearer {your_token}`
3. Send 버튼 클릭
4. 응답 확인

### 쉘 스크립트 테스트
```bash
#!/bin/bash

# JWT 토큰 설정 (실제 토큰으로 교체)
TOKEN="your_jwt_token_here"

# API 호출
curl -X GET "http://localhost:8080/api/profiles/me" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  | jq '.'
```

---

## 업데이트 이력

- **2024-12-08**: 초기 문서 작성
  - GET /api/profiles/me 엔드포인트 문서화
  - 사용 예시 및 활용 시나리오 추가

