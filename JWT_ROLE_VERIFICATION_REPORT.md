# JWT ROLE 포함 여부 검증 및 수정 보고서

## 날짜: 2025년 12월 8일

## 1. 검증 결과

### ❌ **문제 발견: JWT에 ROLE이 포함되지 않음**

기존 코드에서는 OAuth2 로그인 성공 시 **ROLE 정보가 JWT에 포함되지 않았습니다**.

---

## 2. 문제 원인 분석

### 2.1 JWT 생성 메서드 (JwtTokenProvider.java)

두 가지 `createToken` 메서드가 존재:

#### ❌ 기존 사용 메서드 (ROLE 미포함)
```java
public String createToken(Long userId, String email) {
    Date now = new Date();
    Date validity = new Date(now.getTime() + validityInMilliseconds);

    return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("email", email)
            .issuedAt(now)
            .expiration(validity)
            .signWith(key)
            .compact();
}
```
- ❌ `role` claim 없음
- ✅ `email` claim만 포함

#### ✅ ROLE 포함 메서드
```java
public String createToken(String email, String role) {
    Date now = new Date();
    Date validity = new Date(now.getTime() + validityInMilliseconds);

    return Jwts.builder()
            .subject(email)
            .claim("email", email)
            .claim("role", role)  // ✅ ROLE 포함
            .issuedAt(now)
            .expiration(validity)
            .signWith(key)
            .compact();
}
```

### 2.2 OAuth2 로그인 성공 핸들러 (OAuth2AuthenticationSuccessHandler.java)

#### ❌ 기존 코드
```java
protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                    Authentication authentication) {
    CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
    
    // ❌ ROLE이 포함되지 않는 메서드 사용
    String token = jwtTokenProvider.createToken(oAuth2User.getId(), oAuth2User.getEmail());
    
    return UriComponentsBuilder.fromUriString(redirectUri)
            .queryParam("token", token)
            .queryParam("role", oAuth2User.getRole())  // URL 파라미터로만 전달
            .build().toUriString();
}
```

### 2.3 JWT 인증 필터 (JwtAuthenticationFilter.java)

```java
String email = jwtTokenProvider.getEmailFromToken(token);
String role = jwtTokenProvider.getUserRole(token);  // JWT에 role이 없으면 "USER" 반환

UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
        email,
        null,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
);
```

**문제점**: JWT에 `role` claim이 없으면 `getUserRole()`이 기본값 "USER"를 반환하므로, ADMIN 사용자도 USER 권한으로 인증됨.

---

## 3. 수정 내용

### ✅ OAuth2AuthenticationSuccessHandler.java 수정

```java
protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                    Authentication authentication) {
    CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
    
    // ✅ ROLE이 포함된 JWT 생성 메서드 사용
    String token = jwtTokenProvider.createToken(oAuth2User.getEmail(), oAuth2User.getRole());
    
    return UriComponentsBuilder.fromUriString(redirectUri)
            .queryParam("token", token)
            .queryParam("role", oAuth2User.getRole())
            .build().toUriString();
}
```

**변경 사항**:
- `createToken(userId, email)` → `createToken(email, role)`
- 이제 JWT에 `role` claim이 포함됨

---

## 4. 검증 플로우

### 4.1 OAuth2 로그인 플로우

1. **Google OAuth2 로그인**
   - 사용자가 Google 계정으로 로그인

2. **CustomOAuth2UserService**
   ```java
   Role role = ADMIN_EMAILS.contains(email) ? Role.ADMIN : Role.USER;
   User user = User.builder()
           .schoolEmail(email)
           .role(role)  // DB에 ROLE 저장
           .build();
   ```

3. **OAuth2AuthenticationSuccessHandler**
   ```java
   // ✅ JWT에 ROLE 포함
   String token = jwtTokenProvider.createToken(email, role);
   ```

4. **JWT 구조 (수정 후)**
   ```json
   {
     "sub": "user@bssm.hs.kr",
     "email": "user@bssm.hs.kr",
     "role": "ADMIN",  // ✅ ROLE 포함
     "iat": 1234567890,
     "exp": 1234571490
   }
   ```

### 4.2 API 요청 인증 플로우

1. **클라이언트 요청**
   ```
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```

2. **JwtAuthenticationFilter**
   ```java
   String role = jwtTokenProvider.getUserRole(token);  // ✅ JWT에서 "ADMIN" 추출
   
   UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
           email,
           null,
           Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))  // ✅ 올바른 권한 설정
   );
   ```

3. **SecurityConfig**
   ```java
   @PreAuthorize("hasRole('ADMIN')")  // ✅ 정상 작동
   ```

---

## 5. ADMIN 권한이 필요한 사용자

현재 설정된 ADMIN 이메일 (CustomOAuth2UserService.java):

```java
private static final List<String> ADMIN_EMAILS = Arrays.asList(
        "24.051@bssm.hs.kr",
        "24.058@bssm.hs.kr"
);
```

---

## 6. 테스트 방법

### 6.1 JWT 내용 확인

1. **로그인 후 JWT 토큰 받기**
   - Google OAuth2 로그인
   - Redirect URL에서 token 파라미터 확인

2. **JWT 디코딩** (https://jwt.io 사용)
   ```json
   {
     "sub": "24.051@bssm.hs.kr",
     "email": "24.051@bssm.hs.kr",
     "role": "ADMIN",  // ✅ 반드시 있어야 함
     "iat": 1234567890,
     "exp": 1234571490
   }
   ```

### 6.2 ADMIN API 테스트

```bash
# ADMIN 권한 필요한 API 호출
curl -X GET http://localhost:8080/api/admin/chatrooms \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 응답 예상:
# - ADMIN 계정: 200 OK + 채팅방 목록
# - USER 계정: 403 Forbidden
```

---

## 7. 수정 전후 비교

| 항목 | 수정 전 | 수정 후 |
|------|---------|---------|
| JWT에 ROLE 포함 | ❌ 없음 | ✅ 있음 |
| ADMIN 인증 | ❌ 항상 USER 권한 | ✅ ADMIN 권한 정상 작동 |
| @PreAuthorize | ❌ 작동 안 함 | ✅ 정상 작동 |
| JWT subject | userId | email |

---

## 8. 결론

✅ **수정 완료**: JWT 생성 시 ROLE이 정상적으로 포함되도록 수정했습니다.

### 주요 변경 사항
- `OAuth2AuthenticationSuccessHandler`에서 ROLE을 포함하는 JWT 생성 메서드 사용
- JWT에 `role` claim이 포함되어 ADMIN 권한 검증이 정상적으로 작동

### 다음 단계
1. 애플리케이션 재시작
2. Google OAuth2 로그인 테스트
3. JWT 토큰 디코딩하여 `role` claim 확인
4. ADMIN API 접근 권한 테스트

