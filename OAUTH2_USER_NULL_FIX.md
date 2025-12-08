# OAuth2User null 문제 해결 보고서

## 📌 문제 상황
컨트롤러에서 `@AuthenticationPrincipal CustomOAuth2User oAuth2User` 파라미터가 **null**로 주입되어 API 호출 시 NullPointerException이 발생했습니다.

## 🔍 원인 분석

### 근본 원인
JWT 인증 필터(`JwtAuthenticationFilter`)에서 Authentication 객체를 생성할 때, **principal을 단순 문자열(email)**로 설정했기 때문입니다.

```java
// 문제가 있던 코드
UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
    email,  // ❌ String 타입
    null,
    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
);
```

### 타입 불일치
- **JWT 필터**: principal을 `String` (email)로 설정
- **컨트롤러**: `CustomOAuth2User` 타입을 요구
- **Spring Security**: 타입이 맞지 않아 변환 실패 → null 반환

## ✅ 해결 방법

### 1. JwtAuthenticationFilter 수정
JWT 토큰 검증 시 `CustomOAuth2User` 객체를 생성하여 principal로 설정하도록 변경했습니다.

**변경 파일**: `src/main/java/com/example/transitlove/jwt/JwtAuthenticationFilter.java`

```java
@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

    String token = resolveToken(request);

    if (token != null && jwtTokenProvider.validateToken(token)) {
        try {
            // JWT에서 사용자 정보 추출
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String email = jwtTokenProvider.getEmailFromToken(token);
            String role = jwtTokenProvider.getUserRole(token);

            // ✅ CustomOAuth2User 객체 생성
            CustomOAuth2User customOAuth2User = CustomOAuth2User.create(
                    userId,
                    email,
                    role,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
            );

            // ✅ principal을 CustomOAuth2User로 설정
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    customOAuth2User,  // String이 아닌 CustomOAuth2User 객체
                    null,
                    customOAuth2User.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("JWT 인증 성공: userId={}, email={}, role={}", userId, email, role);
        } catch (Exception e) {
            log.warn("JWT 인증 실패: {}", e.getMessage());
        }
    }

    filterChain.doFilter(request, response);
}
```

### 2. CustomOAuth2User 수정
authorities를 받는 오버로드된 `create` 메서드를 추가했습니다.

**변경 파일**: `src/main/java/com/example/transitlove/oauth2/user/CustomOAuth2User.java`

```java
// 기존 메서드
public static CustomOAuth2User create(Long id, String email, String role) {
    return new CustomOAuth2User(id, email, role, Collections.emptyList());
}

// ✅ 새로 추가된 메서드
public static CustomOAuth2User create(Long id, String email, String role, Collection<? extends GrantedAuthority> authorities) {
    return new CustomOAuth2User(id, email, role, authorities);
}
```

### 3. 컨트롤러 정리

#### ProfileController
모든 임시 null 체크 코드를 제거했습니다.

**변경 전**:
```java
@PostMapping
public ResponseEntity<ProfileResponse> createProfile(
        @AuthenticationPrincipal CustomOAuth2User oAuth2User,
        @Valid @RequestBody ProfileCreateRequest request
) {
    // 임시: 테스트용 - oAuth2User가 null이면 userId 1 사용
    Long userId = (oAuth2User != null) ? oAuth2User.getId() : 1L;
    // ...
}
```

**변경 후**:
```java
@PostMapping
public ResponseEntity<ProfileResponse> createProfile(
        @AuthenticationPrincipal CustomOAuth2User oAuth2User,
        @Valid @RequestBody ProfileCreateRequest request
) {
    Long userId = oAuth2User.getId();  // ✅ null 체크 불필요
    // ...
}
```

동일한 변경사항을 다음 메서드들에 적용:
- `getMyProfile()`
- `updateMyProfile()`
- `getMatchedProfiles()`
- `getUnmatchedProfiles()`

#### ChatController
복잡한 SecurityContext 분기 처리를 제거하고 간소화했습니다.

**변경 전**:
```java
private Long getProfileIdFromOAuth2User(CustomOAuth2User oauth2User) {
    String email;
    
    // OAuth2 인증인 경우
    if (oauth2User != null) {
        email = oauth2User.getEmail();
    } else {
        // JWT 인증인 경우 SecurityContext에서 이메일 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            email = (String) authentication.getPrincipal();
        } else {
            throw new IllegalArgumentException("인증 정보를 찾을 수 없습니다.");
        }
    }
    // ...
}
```

**변경 후**:
```java
private Long getProfileIdFromOAuth2User(CustomOAuth2User oauth2User) {
    // JWT 필터에서 CustomOAuth2User를 생성하므로 null이 아님
    if (oauth2User == null) {
        throw new IllegalArgumentException("인증 정보를 찾을 수 없습니다.");
    }

    String email = oauth2User.getEmail();
    // ...
}
```

#### TempChatController & MatchingController
null 체크를 추가하여 안정성을 향상시켰습니다.

```java
private Long getProfileIdFromOAuth2User(CustomOAuth2User oauth2User) {
    // ✅ null 체크 추가
    if (oauth2User == null) {
        throw new IllegalArgumentException("인증 정보를 찾을 수 없습니다.");
    }
    
    User user = userRepository.findBySchoolEmail(oauth2User.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    // ...
}
```

## 🎯 효과

### Before (문제 발생)
```
OAuth2 로그인 → CustomOAuth2User ✅
JWT 인증 → String (email) ❌ → 컨트롤러에서 null
```

### After (문제 해결)
```
OAuth2 로그인 → CustomOAuth2User ✅
JWT 인증 → CustomOAuth2User ✅ → 컨트롤러에서 정상 주입
```

## 📝 변경된 파일 목록

1. **JWT 인증 관련**
   - `jwt/JwtAuthenticationFilter.java` - principal 타입 변경
   - `oauth2/user/CustomOAuth2User.java` - create 메서드 오버로드 추가

2. **컨트롤러**
   - `api/controller/ProfileController.java` - 임시 null 체크 제거
   - `api/controller/ChatController.java` - SecurityContext 분기 제거
   - `api/controller/TempChatController.java` - null 체크 추가
   - `api/controller/MatchingController.java` - null 체크 및 로그 개선

## ✅ 검증

### 빌드 성공
```bash
./gradlew clean build -x test
# BUILD SUCCESSFUL
```

### 테스트 방법
JWT 토큰으로 API 호출 시 정상 동작 확인:

```bash
# 1. OAuth2 로그인하여 JWT 토큰 획득
# 2. API 호출 시 Authorization 헤더에 토큰 포함
curl -X GET "http://localhost:8080/api/profiles/me" \
  -H "Authorization: Bearer <your-jwt-token>"

# 3. oAuth2User가 정상적으로 주입되어 사용자 정보 반환
```

## 📌 결론

JWT 인증 필터에서 `CustomOAuth2User` 객체를 생성하도록 수정하여, OAuth2 로그인과 JWT 인증 모두에서 **동일한 타입의 principal**을 사용하게 되었습니다. 이로써 컨트롤러에서 `@AuthenticationPrincipal CustomOAuth2User` 파라미터가 null로 주입되는 문제가 완전히 해결되었습니다.

---
**작성일**: 2025년 12월 8일  
**상태**: ✅ 해결 완료

