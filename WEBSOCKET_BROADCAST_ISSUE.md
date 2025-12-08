?# 🔍 WebSocket 브로드캐스팅 문제 진단 및 해결

**문제**: 다른 컴퓨터에서 WebSocket 브로드캐스팅이 안 됨  
**원인**: 각자 로컬에서 서버를 실행 중 (서로 다른 서버)  
**해결**: ✅ **백엔드를 한 곳에 배포하면 즉시 해결!**

---

## 🎯 가장 간단한 해결책: 백엔드 배포 ⭐⭐⭐

### 현재 상황 (문제)

```
컴퓨터 A                          컴퓨터 B
├── 백엔드 서버 (localhost:8080)  ├── 백엔드 서버 (localhost:8080)
│   └── 메모리 브로커             │   └── 메모리 브로커
│                                │
├── 프론트엔드                    ├── 프론트엔드
│   └── ws://localhost:8080      │   └── ws://localhost:8080
│       (자기 서버에 연결)         │       (자기 서버에 연결)
```

❌ **서로 다른 서버** → 브로드캐스트 전달 안 됨!

### 해결 후 (배포)

```
        배포된 백엔드 서버
        (예: https://api.transitlove.com)
              └── 메모리 브로커 (공유!)
                 ↙        ↘
         컴퓨터 A          컴퓨터 B
        프론트엔드        프론트엔드
```

✅ **같은 서버** → 브로드캐스트 작동! 🎉

---

## 🐛 문제 원인 (상세)

### 지금 상황
- 각자 `./gradlew bootRun`으로 로컬 서버 실행
- 각자 `localhost:8080`에 연결
- **물리적으로 다른 서버** = 메모리 공유 안 됨

### SimpleBroker의 특성
```java
config.enableSimpleBroker("/topic", "/queue");
```

- ✅ 인메모리 브로커 (빠름, 간단)
- ✅ **단일 서버 인스턴스 내에서는 완벽**
- ❌ 서버 간 통신 불��능 (메모리는 공유되지 않음)

---

## 💡 해결 방법

### 방법 1: 같은 서버에 연결 (즉시 해결) ⭐ 권장

**모든 클라이언트가 동일한 백엔드 서버에 연결**

#### A. 서버를 한 대만 실행

```bash
# 컴퓨터 A에서만 서버 실행
cd /Users/hanjiwon/Desktop/transitLove/BackEnd/transitLove
./gradlew bootRun
```

#### B. 모든 클라이언트가 같은 IP로 연결

```typescript
// 프론트엔드 설정
const WS_URL = 'ws://192.168.1.100:8080/ws'; // 서버 A의 IP

// 모든 컴퓨터에서 이 주소로 연결
const socket = new SockJS(WS_URL);
```

#### C. 서버 IP 확인

```bash
# 서버 컴퓨터에서 IP 확인
ifconfig | grep "inet " | grep -v 127.0.0.1

# 예: 192.168.1.100
```

#### D. 방화벽 확인

```bash
# macOS 방화벽 확인
sudo /usr/libexec/ApplicationFirewall/socketfilterfw --getglobalstate

# 포트 8080 열기
sudo lsof -i :8080
```

---

### 방법 2: Redis를 사용한 외부 브로커 (영구 해결) 🚀

**여러 서버 인스턴스 간 브로드캐스팅 지원**

#### A. Redis 설치

```bash
# macOS
brew install redis
brew services start redis

# Redis 실행 확인
redis-cli ping
# PONG 출력되면 성공
```

#### B. Gradle 의존성 추가

`build.gradle`에 추가:
```gradle
dependencies {
    // 기존 의존성...
    
    // Redis 메시지 브로커
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'org.springframework.session:spring-session-data-redis'
}
```

#### C. application.properties 설정

```properties
# Redis 설정
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=
```

#### D. WebSocketConfig 수정

```java
@Override
public void configureMessageBroker(MessageBrokerRegistry config) {
    // ❌ SimpleBroker 대신
    // config.enableSimpleBroker("/topic", "/queue");
    
    // ✅ Redis 기반 브로커 사용
    config.enableStompBrokerRelay("/topic", "/queue")
            .setRelayHost("localhost")
            .setRelayPort(61613)  // STOMP 프로토콜 포트
            .setClientLogin("guest")
            .setClientPasscode("guest")
            .setSystemLogin("guest")
            .setSystemPasscode("guest");
    
    config.setApplicationDestinationPrefixes("/app");
}
```

**주의**: STOMP 프로토콜을 지원하는 RabbitMQ를 사용하는 것이 더 일반적입니다.

---

### 방법 3: RabbitMQ를 사용한 외부 브로커 (가장 권장) 🎯

#### A. RabbitMQ 설치

```bash
# macOS
brew install rabbitmq
brew services start rabbitmq

# RabbitMQ 관리 콘솔
# http://localhost:15672
# 기본 계정: guest / guest
```

#### B. Gradle 의존성 추가

`build.gradle`:
```gradle
dependencies {
    // 기존 의존성...
    
    // RabbitMQ 메시지 브로커
    implementation 'org.springframework.boot:spring-boot-starter-amqp'
}
```

#### C. application.properties 설정

```properties
# RabbitMQ 설정
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

#### D. WebSocketConfig 수정

```java
@Override
public void configureMessageBroker(MessageBrokerRegistry config) {
    // ❌ SimpleBroker 대신
    // config.enableSimpleBroker("/topic", "/queue");
    
    // ✅ RabbitMQ 기반 브로커 사용
    config.enableStompBrokerRelay("/topic", "/queue")
            .setRelayHost("localhost")
            .setRelayPort(61613)
            .setClientLogin("guest")
            .setClientPasscode("guest")
            .setSystemLogin("guest")
            .setSystemPasscode("guest");
    
    config.setApplicationDestinationPrefixes("/app");
}
```

#### E. RabbitMQ STOMP 플러그인 활성화

```bash
# STOMP 플러그인 활성화
rabbitmq-plugins enable rabbitmq_stomp

# 재시작
brew services restart rabbitmq
```

---

## 🔍 진단 도구

### 1. WebSocket 연결 상태 확인 API

```java
@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class WebSocketDebugController {
    
    private final SimpUserRegistry userRegistry;
    
    @GetMapping("/ws/connections")
    public ResponseEntity<?> getConnections() {
        Map<String, Object> info = new HashMap<>();
        info.put("totalUsers", userRegistry.getUserCount());
        info.put("users", userRegistry.getUsers().stream()
                .map(SimpUser::getName)
                .collect(Collectors.toList()));
        return ResponseEntity.ok(info);
    }
}
```

### 2. 브로드캐스트 테스트 API

```java
@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class BroadcastTestController {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    @PostMapping("/broadcast/test")
    public ResponseEntity<?> testBroadcast(@RequestParam Long chatRoomId) {
        VoiceSignalMessage testMessage = VoiceSignalMessage.builder()
                .type(VoiceSignalType.ERROR)
                .chatRoomId(chatRoomId)
                .errorMessage("테스트 브로드캐스트 메시지")
                .timestamp(System.currentTimeMillis())
                .build();
        
        messagingTemplate.convertAndSend("/topic/voice." + chatRoomId, testMessage);
        
        return ResponseEntity.ok("브로드캐스트 전송 완료");
    }
}
```

### 3. 로깅 강화

`application.properties`:
```properties
# WebSocket 디버그 로깅
logging.level.org.springframework.messaging=DEBUG
logging.level.org.springframework.web.socket=DEBUG
logging.level.com.example.transitlove.service.VoiceChatService=DEBUG
```

---

## 📊 문제 상황별 체크리스트

### ✅ 확인 사항

#### 1. 서버 연결 확인
```bash
# 서버 A에서
curl http://localhost:8080/actuator/health

# 서버 B에서
curl http://서버A_IP:8080/actuator/health
```

#### 2. 방화벽 확인
```bash
# 포트 8080이 열려있는지 확인
sudo lsof -i :8080
netstat -an | grep 8080
```

#### 3. 네트워크 연결 확인
```bash
# 서버 B에서 서버 A로 핑
ping 서버A_IP

# 텔넷으로 포트 확인
telnet 서버A_IP 8080
```

#### 4. WebSocket 연결 URL 확인
```typescript
// 프론트엔드에서
console.log('WebSocket URL:', WS_URL);
// 모두 같은 서버를 가리켜야 함!
```

---

## 🚀 즉시 해결 방법 (추천)

### Step 1: 한 대의 서버만 실행

```bash
# 컴퓨터 A (서버 컴퓨터)
cd /Users/hanjiwon/Desktop/transitLove/BackEnd/transitLove
./gradlew bootRun
```

### Step 2: 서버 IP 확인

```bash
# 컴퓨터 A에서
ifconfig | grep "inet " | grep -v 127.0.0.1
# 예: inet 192.168.1.100
```

### Step 3: 모든 클라이언트가 이 IP로 연결

```typescript
// voice-signaling-test.html 또는 프론트엔드 설정
const WS_URL = 'ws://192.168.1.100:8080/ws'; // 서버 A의 IP
```

### Step 4: 테스트

```
컴퓨터 A: http://192.168.1.100:8080/voice-signaling-test.html
컴퓨터 B: http://192.168.1.100:8080/voice-signaling-test.html
```

둘 다 **같은 서버**에 연결됨 → 브로드캐스트 작동! ✅

---

## 🎯 장기 해결 방안

### 프로덕션 환경

1. **로드 밸런서 + 외부 브로커**
   ```
   클라이언트들
        ↓
   로드 밸런서
    ↙     ↘
   서버1   서버2
    ↘     ↙
    RabbitMQ
   ```

2. **Kubernetes + Redis Pub/Sub**
   ```
   클라이언트들
        ↓
   Ingress
        ↓
   Service (서버 Pod들)
        ↓
   Redis Cluster
   ```

---

## 🧪 테스트 시나리오

### 현재 상황 테스트

```bash
# 1. 서버 로그 확인
tail -f server.log | grep "브로드캐스트"

# 2. 연결된 사용자 확인
curl http://localhost:8080/api/debug/ws/connections

# 3. 강제 브로드캐스트 테스트
curl -X POST http://localhost:8080/api/debug/broadcast/test?chatRoomId=1
```

---

## 📝 요약

### 문제의 핵심
```
SimpleBroker = 인메모리 = 단일 서버만 지원
다른 컴퓨터 = 다른 서버 = 브로드캐스트 전달 안 됨
```

### 해결책

| 방법 | 난이도 | 시간 | 서버 간 지원 |
|------|--------|------|-------------|
| **같은 서버 연결** | ⭐ 쉬움 | 즉시 | ❌ (서버 1대만) |
| **RabbitMQ** | ⭐⭐⭐ 보통 | 30분 | ✅ (여러 서버) |
| **Redis** | ⭐⭐ 보통 | 20분 | ✅ (여러 서버) |

### 추천
1. **개발 환경**: 같은 서버에 연결 (즉시 해결)
2. **프로덕션**: RabbitMQ 사용 (확장성)

---

**작성 일자**: 2025-12-09  
**문제**: SimpleBroker 제한  
**상태**: 해결 방법 제시

