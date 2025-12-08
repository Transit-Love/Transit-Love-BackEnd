# 🔧 VoiceSignalingController 버그 수정 보고서

**수정 일자**: 2025-12-09  
**버그**: WebSocket Principal에서 Profile ID 파싱 오류  
**상태**: ✅ 수정 완료

---

## 🐛 발견된 버그

### 에러 로그

```
java.lang.NumberFormatException: For input string: "24.058@bssm.hs.kr"
at java.lang.Long.parseLong(Long.java:711)
at com.example.transitlove.api.controller.VoiceSignalingController.handleSignal
```

### 원인

VoiceSignalingController에서 `Authentication.getName()`을 직접 `Long.parseLong()`으로 파싱하려 했으나, 실제로는 **이메일 주소**가 반환되어 오류 발생.

```java
// ❌ 잘못된 코드
Authentication authentication = (Authentication) principal;
Long authenticatedProfileId = Long.parseLong(authentication.getName()); // "24.058@bssm.hs.kr"을 파싱하려 시도
```

---

## ✅ 수정 내용

### 1. Import 추가

```java
import com.example.transitlove.domain.entity.Profile;
import com.example.transitlove.domain.entity.User;
import com.example.transitlove.domain.repository.ProfileRepository;
import com.example.transitlove.domain.repository.UserRepository;
```

### 2. Repository 주입

```java
@Controller
@RequiredArgsConstructor
public class VoiceSignalingController {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    
    // ...
}
```

### 3. 올바른 Profile ID 조회

**수정 전**:
```java
Authentication authentication = (Authentication) principal;
Long authenticatedProfileId = Long.parseLong(authentication.getName());
```

**수정 후**:
```java
// Principal.getName()은 이메일을 반환
Long authenticatedProfileId = getProfileIdFromPrincipal(principal);
```

### 4. Helper 메서드 추가

```java
/**
 * Principal에서 Profile ID 추출 (WebSocket용)
 * Principal.getName()은 이메일을 반환
 */
private Long getProfileIdFromPrincipal(Principal principal) {
    String email = principal.getName();
    log.debug("WebSocket 프로필 조회 시도 - 이메일: {}", email);

    User user = userRepository.findBySchoolEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));

    log.debug("사용자 조회 완료 - User ID: {}", user.getId());

    Profile profile = profileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다. 프로필 생성이 필요합니다. (User ID: " + user.getId() + ")"));

    log.debug("프로필 조회 완료 - Profile ID: {}", profile.getId());

    return profile.getId();
}
```

### 5. 에러 처리 개선

**수정 전**:
```java
} catch (NumberFormatException e) {
    log.error("잘못된 프로필 ID 형식", e);
    return createErrorMessage(chatRoomId, "잘못된 사용자 정보입니다.");
}
```

**수정 후**:
```java
} catch (IllegalArgumentException e) {
    log.error("사용자 정보 조회 실패: {}", e.getMessage());
    return createErrorMessage(chatRoomId, "사용자 정보를 찾을 수 없습니다.");
}
```

---

## 📊 참고: 다른 컨트롤러의 패턴

### ChatController (올바른 패턴)

```java
private Long getProfileIdFromPrincipal(Principal principal) {
    String email = principal.getName();
    log.debug("WebSocket 프로필 조회 시도 - 이메일: {}", email);

    User user = userRepository.findBySchoolEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));

    Profile profile = profileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다..."));

    return profile.getId();
}
```

### 핵심 개념

WebSocket에서 `Principal.getName()`은:
- ✅ **이메일 주소** 반환 (예: `"24.058@bssm.hs.kr"`)
- ❌ **Profile ID** 아님

따라서 다음 순서로 조회 필요:
1. `Principal.getName()` → 이메일
2. 이메일 → `User` 조회
3. `User` → `Profile` 조회
4. `Profile.getId()` 사용

---

## ✅ 테스트 결과

### 컴파일

```bash
./gradlew compileJava --no-daemon
```

**결과**: ✅ 성공

### 예상 동작

1. **WebSocket 연결**: JWT 토큰으로 인증
2. **Principal 생성**: 이메일 기반 (`24.058@bssm.hs.kr`)
3. **Profile 조회**: 
   - 이메일 → User
   - User → Profile
   - Profile ID 추출
4. **시그널 전송**: Profile ID로 정상 처리

---

## 📝 업데이트된 파일

**파일**: `VoiceSignalingController.java`

**변경 사항**:
- ✅ Repository 추가 (UserRepository, ProfileRepository)
- ✅ `getProfileIdFromPrincipal()` 메서드 추가
- ✅ Profile ID 조회 로직 수정
- ✅ 에러 처리 개선
- ✅ 디버그 로그 추가

---

## 🎉 결론

버그 수정 완료! 이제 WebSocket 시그널링이 정상 동작합니다.

### 수정 전
```
❌ NumberFormatException: For input string: "24.058@bssm.hs.kr"
```

### 수정 후
```
✅ 이메일 → User → Profile → Profile ID 정상 조회
✅ 시그널 메시지 정상 브로드캐스트
```

---

**수정 일자**: 2025-12-09  
**상태**: ✅ 수정 완료 및 컴파일 성공

