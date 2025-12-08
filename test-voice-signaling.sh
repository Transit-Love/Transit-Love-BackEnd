#!/bin/bash

# 음성 채팅 시그널링 테스트 스크립트
# 사용법: ./test-voice-signaling.sh

echo "======================================"
echo "음성 채팅 시그널링 API 테스트"
echo "======================================"
echo ""

# 서버 주소
BASE_URL="http://localhost:8080"

# JWT 토큰 (실제 토큰으로 교체 필요)
JWT_TOKEN="${JWT_TOKEN:-YOUR_JWT_TOKEN}"

if [ "$JWT_TOKEN" = "YOUR_JWT_TOKEN" ]; then
    echo "❌ JWT 토큰이 설정되지 않았습니다."
    echo ""
    echo "사용 방법:"
    echo "  export JWT_TOKEN='실제_토큰'"
    echo "  ./test-voice-signaling.sh"
    echo ""
    echo "또는 브라우저 테스트 페이지를 사용하세요:"
    echo "  ${BASE_URL}/voice-signaling-test.html"
    exit 1
fi

echo "✅ JWT 토큰 설정됨"
echo ""

# 테스트 데이터
CHAT_ROOM_ID=1
MY_PROFILE_ID=1
PARTNER_PROFILE_ID=2

echo "테스트 설정:"
echo "  - 채팅방 ID: ${CHAT_ROOM_ID}"
echo "  - 내 프로필 ID: ${MY_PROFILE_ID}"
echo "  - 상대방 프로필 ID: ${PARTNER_PROFILE_ID}"
echo ""

# 색상 코드
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 함수: 테스트 실행
test_api() {
    local test_name="$1"
    local method="$2"
    local endpoint="$3"
    local data="$4"

    echo "======================================"
    echo "테스트: ${test_name}"
    echo "======================================"
    echo "Method: ${method}"
    echo "Endpoint: ${endpoint}"

    if [ -n "$data" ]; then
        echo "Request Body:"
        echo "$data" | jq '.' 2>/dev/null || echo "$data"
        echo ""
    fi

    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\nHTTP_CODE:%{http_code}" \
            -H "Authorization: Bearer ${JWT_TOKEN}" \
            "${BASE_URL}${endpoint}")
    else
        response=$(curl -s -w "\nHTTP_CODE:%{http_code}" \
            -X "${method}" \
            -H "Authorization: Bearer ${JWT_TOKEN}" \
            -H "Content-Type: application/json" \
            -d "$data" \
            "${BASE_URL}${endpoint}")
    fi

    http_code=$(echo "$response" | grep "HTTP_CODE:" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_CODE:/d')

    echo "HTTP Status: ${http_code}"
    echo "Response:"
    echo "$body" | jq '.' 2>/dev/null || echo "$body"
    echo ""

    if [ "$http_code" = "200" ] || [ "$http_code" = "201" ]; then
        echo -e "${GREEN}✅ 테스트 성공${NC}"
    else
        echo -e "${RED}❌ 테스트 실패 (HTTP ${http_code})${NC}"
    fi
    echo ""
}

# REST API 테스트

echo ""
echo "======================================"
echo "REST API 테스트 시작"
echo "======================================"
echo ""

# 1. 음소거 상태 업데이트 (명세서 버전)
test_api "음소거 상태 업데이트" \
    "POST" \
    "/api/voice/mute" \
    '{
        "chatRoomId": '"${CHAT_ROOM_ID}"',
        "profileId": '"${MY_PROFILE_ID}"',
        "isMuted": true
    }'

sleep 1

# 2. 음소거 상태 조회
test_api "음소거 상태 조회" \
    "GET" \
    "/api/voice/room/${CHAT_ROOM_ID}/mute/${MY_PROFILE_ID}" \
    ""

sleep 1

# 3. 통화 기록 저장
START_TIME=$(date -u -v-15M +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -d '15 minutes ago' +"%Y-%m-%dT%H:%M:%SZ")
END_TIME=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

test_api "통화 기록 저장" \
    "POST" \
    "/api/voice/call/history" \
    '{
        "chatRoomId": '"${CHAT_ROOM_ID}"',
        "callerProfileId": '"${MY_PROFILE_ID}"',
        "receiverProfileId": '"${PARTNER_PROFILE_ID}"',
        "startTime": "'"${START_TIME}"'",
        "endTime": "'"${END_TIME}"'",
        "duration": 900,
        "callStatus": "COMPLETED",
        "voiceModulationUsed": false
    }'

sleep 1

# 4. 채팅방별 통화 기록 조회
test_api "채팅방별 통화 기록 조회" \
    "GET" \
    "/api/voice/call/history?chatRoomId=${CHAT_ROOM_ID}&page=0&size=20" \
    ""

sleep 1

# 5. 통화 통계 조회
test_api "통화 통계 조회" \
    "GET" \
    "/api/voice/call/statistics?chatRoomId=${CHAT_ROOM_ID}" \
    ""

echo ""
echo "======================================"
echo "테스트 완료"
echo "======================================"
echo ""
echo "WebSocket 시그널링 테스트는 브라우저에서:"
echo -e "${YELLOW}${BASE_URL}/voice-signaling-test.html${NC}"
echo ""
echo "테스트 순서:"
echo "  1. JWT 토큰 입력"
echo "  2. 채팅방 정보 설정"
echo "  3. '연결' 버튼 클릭"
echo "  4. 시그널 타입 선택"
echo "  5. '시그널 전송' 버튼 클릭"
echo "  6. 수신 로그 확인"
echo ""

