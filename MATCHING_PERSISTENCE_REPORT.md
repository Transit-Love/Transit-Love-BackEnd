# 매칭 데이터 영속성 검증 및 개선 보고서

## 날짜: 2025년 12월 8일

---

## ✅ 질문: "서버 킬 때마다 매칭이 바뀌는거 아니지?"

### 답변: **아니요, 매칭은 바뀌지 않습니다! 한번 매칭되면 DB에 영구 저장됩니다.**

---

## 🔍 검증 결과

### 1. ✅ 데이터베이스 설정 확인

#### application.properties
```properties
spring.jpa.hibernate.ddl-auto=update
```

**상태**: ✅ **완벽**
- `update` 모드: 기존 데이터 유지, 스키마만 업데이트
- `create` 또는 `create-drop`이 아니므로 데이터 삭제 안됨
- 서버 재시작해도 **모든 데이터 보존**

---

### 2. ✅ Match 엔티티 확인

```java
@Entity
@Table(name = "matches")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile1_id", nullable = false)
    private Profile profile1;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile2_id", nullable = false)
    private Profile profile2;
    
    @Column(nullable = false)
    private Boolean isActive;
    
    @Column(nullable = false)
    private Double similarityScore;
    
    @Column(nullable = false)
    private LocalDateTime matchedAt;
    
    private LocalDateTime unmatchedAt;
    
    // ...
}
```

**상태**: ✅ **완벽**
- DB 테이블 `matches`에 저장
- `isActive` 필드로 활성/비활성 관리
- `matchedAt`, `unmatchedAt`으로 이력 관리
- 서버 재시작해도 **DB에서 자동 로드**

---

### 3. ✅ MatchingService 로직 확인

```java
@Transactional
public List<Match> autoMatchAllProfiles() {
    log.info("전체 프로필 자동 매칭 시작");
    
    // 1. 이미 매칭된 프로필 ID 조회
    List<Match> existingMatches = matchRepository.findAll().stream()
            .filter(Match::getIsActive)
            .collect(Collectors.toList());
    
    List<Long> matchedProfileIds = existingMatches.stream()
            .flatMap(match -> List.of(match.getProfile1().getId(), match.getProfile2().getId()).stream())
            .distinct()
            .collect(Collectors.toList());
    
    // 2. 미매칭 프로필만 조회
    List<Profile> unmatchedProfiles = profileRepository.findAll().stream()
            .filter(profile -> !matchedProfileIds.contains(profile.getId()))
            .collect(Collectors.toList());
    
    // 3. 미매칭 프로필만 매칭
    List<Match> newMatches = autoMatchingDomainService.createOptimalMatches(unmatchedProfiles);
    
    // 4. 매칭 저장
    return matchRepository.saveAll(newMatches);
}
```

**상태**: ✅ **완벽**
- 기존 매칭을 먼저 확인
- **이미 매칭된 프로필은 절대 다시 매칭 안됨**
- 새로운 프로필만 매칭 대상

---

### 4. ⚠️ DataLoader 개선 (수정 완료)

#### 기존 코드:
```java
// 자동 매칭 실행
try {
    log.info("자동 매칭 시작...");
    List<Match> matches = matchingService.autoMatchAllProfiles();
    // ...
} catch (Exception e) {
    log.warn("자동 매칭 실패: {}", e.getMessage());
}
```

**문제점**:
- 서버 재시작할 때마다 `autoMatchAllProfiles()` 호출
- 실제로는 중복 매칭 안되지만, 불필요한 로그와 처리

#### ✅ 개선된 코드:
```java
// 자동 매칭 실행 (초기 데이터 생성 시에만)
// 이미 매칭이 있으면 자동 매칭을 실행하지 않음 (서버 재시작 시 매칭 보존)
if (userRepository.count() <= 2) { // 관리자만 있는 경우 (초기 상태)
    try {
        log.info("자동 매칭 시작...");
        List<Match> matches = matchingService.autoMatchAllProfiles();
        log.info("자동 매칭 완료! {} 개의 커플이 생성되었습니다.", matches.size());
        // ...
    } catch (Exception e) {
        log.warn("자동 매칭 실패: {}", e.getMessage());
    }
} else {
    log.info("기존 사용자 데이터 존재 - 자동 매칭 건너뜀 (서버 재시작)");
}
```

**개선 사항**:
- ✅ 초기 상태(관리자만 있음)일 때만 자동 매칭 실행
- ✅ 서버 재시작 시 자동 매칭 건너뜀
- ✅ 명확한 로그 메시지

---

## 📊 매칭 데이터 흐름

### 1. 첫 서버 시작 (DB 비어있음)

```
1. DataLoader 실행
   ↓
2. 테스트 유저 6명 생성
   ↓
3. 자동 매칭 실행 (3쌍 생성)
   ↓
4. DB에 저장 (matches 테이블)
   
[matches 테이블]
id | profile1_id | profile2_id | isActive | matchedAt
1  | 1           | 2           | true     | 2025-12-08 10:00
2  | 3           | 4           | true     | 2025-12-08 10:00
3  | 5           | 6           | true     | 2025-12-08 10:00
```

### 2. 서버 재시작

```
1. DataLoader 실행
   ↓
2. userRepository.count() > 2 (기존 데이터 있음)
   ↓
3. 자동 매칭 건너뜀 ✅
   ↓
4. DB의 기존 매칭 그대로 유지 ✅

[matches 테이블 - 변경 없음]
id | profile1_id | profile2_id | isActive | matchedAt
1  | 1           | 2           | true     | 2025-12-08 10:00
2  | 3           | 4           | true     | 2025-12-08 10:00
3  | 5           | 6           | true     | 2025-12-08 10:00
```

### 3. 새 유저 추가 후 수동 매칭

```
1. 새 유저 프로필 생성 (id=7, 8)
   ↓
2. API 호출: POST /api/matching/auto
   ↓
3. MatchingService.autoMatchAllProfiles()
   - 기존 매칭 조회 (1,2,3,4,5,6)
   - 미매칭 프로필 조회 (7,8)
   - 새 매칭 생성 (7 ❤️ 8)
   ↓
4. DB에 저장

[matches 테이블]
id | profile1_id | profile2_id | isActive | matchedAt
1  | 1           | 2           | true     | 2025-12-08 10:00
2  | 3           | 4           | true     | 2025-12-08 10:00
3  | 5           | 6           | true     | 2025-12-08 10:00
4  | 7           | 8           | true     | 2025-12-08 11:30 ✅ 새로 추가
```

---

## 🔒 매칭 보존 보장 메커니즘

### 1. 데이터베이스 레벨
- ✅ `spring.jpa.hibernate.ddl-auto=update` - 데이터 보존
- ✅ MySQL RDS 사용 - 영구 저장소
- ✅ 트랜잭션 관리 - 데이터 무결성

### 2. 애플리케이션 레벨
- ✅ `MatchingService.autoMatchAllProfiles()` - 중복 방지 로직
- ✅ `Match.isActive` 플래그 - 활성/비활성 관리
- ✅ `matchedAt`, `unmatchedAt` - 이력 관리

### 3. 비즈니스 로직 레벨
- ✅ 이미 매칭된 프로필 ID 필터링
- ✅ 미매칭 프로필만 매칭 대상
- ✅ 매칭 해제 시 `unmatch()` 메서드 사용 (삭제 아님)

---

## 🧪 검증 테스트

### 테스트 1: 서버 재시작 후 매칭 확인

```bash
# 1. 현재 매칭 조회
curl -X GET "http://localhost:8080/api/profiles/matched" \
  -H "Authorization: Bearer ${TOKEN}"

# 응답 저장
# 예: [{"id":1, "nickname":"김철수"}, {"id":2, "nickname":"이영희"}]

# 2. 서버 재시작
./gradlew bootRun

# 3. 다시 매칭 조회
curl -X GET "http://localhost:8080/api/profiles/matched" \
  -H "Authorization: Bearer ${TOKEN}"

# 예상 결과: ✅ 동일한 매칭 유지
```

### 테스트 2: DB 직접 확인

```sql
-- MySQL에 직접 접속
mysql -h transitlove.covc4wqqyu59.us-east-1.rds.amazonaws.com \
      -u admin -p transitLove

-- 매칭 데이터 확인
SELECT 
    m.id,
    p1.nickname as profile1,
    p2.nickname as profile2,
    m.is_active,
    m.matched_at,
    m.similarity_score
FROM matches m
JOIN profiles p1 ON m.profile1_id = p1.id
JOIN profiles p2 ON m.profile2_id = p2.id
WHERE m.is_active = true;
```

### 테스트 3: 로그 확인

```bash
# 서버 시작 후 로그 확인
tail -f server.log

# 첫 시작 시:
# "자동 매칭 시작..."
# "자동 매칭 완료! 3 개의 커플이 생성되었습니다."

# 재시작 시:
# "기존 사용자 데이터 존재 - 자동 매칭 건너뜀 (서버 재시작)"
```

---

## 📝 매칭 관련 API

### 1. 내 매칭 상대 조회
```http
GET /api/profiles/matched
Authorization: Bearer {token}
```

### 2. 수동 매칭 (ADMIN만)
```http
POST /api/matching/auto
Authorization: Bearer {admin_token}
```

### 3. 특정 프로필 매칭
```http
POST /api/matching/{profileId}
Authorization: Bearer {admin_token}
```

### 4. 매칭 해제 (ADMIN만)
```http
DELETE /api/matching/{matchId}?profileId={profileId}
Authorization: Bearer {admin_token}
```

---

## 🎯 결론

### ✅ **매칭은 절대 바뀌지 않습니다!**

#### 보장되는 것들:

1. **DB 영구 저장**: MySQL RDS에 저장, 서버 재시작해도 유지
2. **중복 매칭 방지**: 이미 매칭된 프로필은 재매칭 안됨
3. **이력 관리**: `matchedAt`, `unmatchedAt`으로 완벽한 이력 보관
4. **트랜잭션 보장**: `@Transactional`로 데이터 무결성 보장

#### 추가 개선 사항:

- ✅ DataLoader 개선: 서버 재시작 시 자동 매칭 건너뜀
- ✅ 명확한 로그 메시지
- ✅ 불필요한 처리 제거

---

## ⚠️ 주의사항

### 매칭이 초기화되는 경우 (의도적인 경우만)

1. **DB 초기화**:
   ```properties
   # application.properties
   spring.jpa.hibernate.ddl-auto=create  # ❌ 절대 사용 금지!
   ```

2. **테이블 수동 삭제**:
   ```sql
   DROP TABLE matches;  -- ❌ 수동으로 삭제하지 않는 한 안전
   ```

3. **DB 전체 삭제**:
   - AWS RDS 인스턴스 삭제

### 현재 설정 (안전):
```properties
spring.jpa.hibernate.ddl-auto=update  # ✅ 안전, 데이터 보존
```

---

## 📌 최종 답변

**질문**: "서버 킬 때마다 매칭이 바뀌는거 아니지?"

**답변**: 
## ✅ **아니요, 절대 바뀌지 않습니다!**

- ✅ DB에 영구 저장 (`spring.jpa.hibernate.ddl-auto=update`)
- ✅ 중복 매칭 방지 로직 구현
- ✅ 서버 재시작 시 자동 매칭 건너뜀 (개선 완료)
- ✅ 한번 매칭된 커플은 DB에서 계속 유지

**커플 매칭은 한번 되면 바뀌지 않습니다!** 🔒💑

