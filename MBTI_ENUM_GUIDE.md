# MBTI ENUM 적용 완료

## 개요
MBTI 필드를 String에서 ENUM으로 변경하여 유효하지 않은 MBTI 값이 입력되지 않도록 개선했습니다.

## 변경 사항

### 1. MBTI ENUM 클래스 생성
- 위치: `com.example.transitlove.domain.enums.MBTI`
- 16가지 MBTI 타입 정의: ISTJ, ISFJ, INFJ, INTJ, ISTP, ISFP, INFP, INTP, ESTP, ESFP, ENFP, ENTP, ESTJ, ESFJ, ENFJ, ENTJ

### 2. Profile 엔티티 수정
- `mbti` 필드 타입을 `String`에서 `MBTI` enum으로 변경
- `@Enumerated(EnumType.STRING)` 어노테이션 추가
- DB에는 문자열로 저장됨

### 3. DTO 수정
- `ProfileCreateRequest`: MBTI 타입으로 변경
- 모든 Response DTO: `profile.getMbti().getValue()`를 사용하여 String으로 변환

### 4. 검증 기능
- Jackson의 `@JsonCreator`를 통해 자동 변환
- 유효하지 않은 MBTI 값이 입력되면 명확한 에러 메시지 반환

## API 사용 예시

### 프로필 생성 요청 (POST /api/profiles)
```json
{
  "nickname": "테스트",
  "mbti": "ENFP",
  "keywordIds": [1, 2, 3],
  "balanceGameAnswers": [
    {
      "balanceGameId": 1,
      "selectedOption": 1
    }
  ]
}
```

### 유효한 MBTI 값
- ISTJ, ISFJ, INFJ, INTJ
- ISTP, ISFP, INFP, INTP
- ESTP, ESFP, ENFP, ENTP
- ESTJ, ESFJ, ENFJ, ENTJ

### 에러 예시
잘못된 MBTI 값 입력 시:
```json
{
  "mbti": "ABCD"
}
```

응답:
```
유효하지 않은 MBTI 타입입니다: ABCD. 허용되는 값: ISTJ, ISFJ, INFJ, INTJ, ISTP, ISFP, INFP, INTP, ESTP, ESFP, ENFP, ENTP, ESTJ, ESFJ, ENFJ, ENTJ
```

## 장점
1. ✅ **타입 안정성**: 컴파일 타임에 유효하지 않은 값 검증
2. ✅ **명확한 에러 메시지**: 잘못된 값 입력 시 허용되는 값 목록 제공
3. ✅ **일관성**: 모든 코드에서 동일한 MBTI 타입 사용
4. ✅ **유지보수성**: 중앙에서 MBTI 타입 관리

## 테스트
```bash
# 빌드
./gradlew clean build -x test

# 서버 실행
./gradlew bootRun
```

## 주의사항
- MBTI 값은 대소문자 구분 없이 입력 가능 (자동으로 대문자로 변환)
- null 값도 허용됨 (선택 사항)
- 기존 DB 데이터는 자동으로 ENUM으로 변환됨

