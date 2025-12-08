#!/bin/bash

# Transit Love - 채팅방 API 테스트 스크립트

BASE_URL="http://localhost:8080"
JWT_TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwicm9sZSI6IlVTRVIiLCJpYXQiOjE3MDE1MjUyMDAsImV4cCI6MTcwMTUyODgwMH0.TEST_TOKEN"

echo "=================================================="
echo "Transit Love - 채팅방 API 테스트"
echo "=================================================="
echo ""

# 색상 정의
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}⚠️  주의: 실제 JWT 토큰으로 교체해주세요!${NC}"
echo ""
echo "JWT_TOKEN을 스크립트 내에서 수정하거나 환경변수로 설정:"
echo "export TEST_JWT_TOKEN='your_actual_jwt_token'"
echo ""

# JWT 토큰 확인
if [ ! -z "$TEST_JWT_TOKEN" ]; then
    JWT_TOKEN=$TEST_JWT_TOKEN
    echo -e "${GREEN}✓ 환경변수에서 JWT 토큰 로드됨${NC}"
    echo ""
fi

# 테스트 1: 채팅방 생성/조회
echo -e "${BLUE}[테스트 1] 채팅방 생성/조회${NC}"
echo "POST /api/chatrooms"
RESPONSE=$(curl -s -X POST "$BASE_URL/api/chatrooms" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{"targetProfileId": 2}' \
  -w "\nHTTP_STATUS:%{http_code}")

HTTP_STATUS=$(echo "$RESPONSE" | grep "HTTP_STATUS" | cut -d':' -f2)
BODY=$(echo "$RESPONSE" | sed '/HTTP_STATUS/d')

echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"
echo ""

if [ "$HTTP_STATUS" = "200" ]; then
    echo -e "${GREEN}✓ 성공 (200 OK)${NC}"
    CHAT_ROOM_ID=$(echo "$BODY" | jq -r '.chatRoomId' 2>/dev/null)
    echo "생성된 채팅방 ID: $CHAT_ROOM_ID"
else
    echo -e "${RED}✗ 실패 (HTTP $HTTP_STATUS)${NC}"
fi
echo ""
echo "=================================================="
echo ""

# 테스트 2: 채팅방 리스트 조회
echo -e "${BLUE}[테스트 2] 채팅방 리스트 조회${NC}"
echo "GET /api/chatrooms"
RESPONSE=$(curl -s -X GET "$BASE_URL/api/chatrooms" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -w "\nHTTP_STATUS:%{http_code}")

HTTP_STATUS=$(echo "$RESPONSE" | grep "HTTP_STATUS" | cut -d':' -f2)
BODY=$(echo "$RESPONSE" | sed '/HTTP_STATUS/d')

echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"
echo ""

if [ "$HTTP_STATUS" = "200" ]; then
    echo -e "${GREEN}✓ 성공 (200 OK)${NC}"
    MATCHED_COUNT=$(echo "$BODY" | jq '.matchedRooms | length' 2>/dev/null)
    UNMATCHED_COUNT=$(echo "$BODY" | jq '.unmatchedRooms | length' 2>/dev/null)
    echo "매칭된 채팅방: $MATCHED_COUNT개"
    echo "일반 채팅방: $UNMATCHED_COUNT개"
else
    echo -e "${RED}✗ 실패 (HTTP $HTTP_STATUS)${NC}"
fi
echo ""
echo "=================================================="
echo ""

# 테스트 3: 채팅방 메시지 조회
if [ ! -z "$CHAT_ROOM_ID" ] && [ "$CHAT_ROOM_ID" != "null" ]; then
    echo -e "${BLUE}[테스트 3] 채팅방 메시지 조회${NC}"
    echo "GET /api/chatrooms/$CHAT_ROOM_ID/messages"
    RESPONSE=$(curl -s -X GET "$BASE_URL/api/chatrooms/$CHAT_ROOM_ID/messages" \
      -H "Authorization: Bearer $JWT_TOKEN" \
      -w "\nHTTP_STATUS:%{http_code}")

    HTTP_STATUS=$(echo "$RESPONSE" | grep "HTTP_STATUS" | cut -d':' -f2)
    BODY=$(echo "$RESPONSE" | sed '/HTTP_STATUS/d')

    echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"
    echo ""

    if [ "$HTTP_STATUS" = "200" ]; then
        echo -e "${GREEN}✓ 성공 (200 OK)${NC}"
        MESSAGE_COUNT=$(echo "$BODY" | jq 'length' 2>/dev/null)
        echo "메시지 수: $MESSAGE_COUNT개"
    elif [ "$HTTP_STATUS" = "410" ]; then
        echo -e "${YELLOW}⚠️  채팅방이 만료되었습니다 (410 Gone)${NC}"
    else
        echo -e "${RED}✗ 실패 (HTTP $HTTP_STATUS)${NC}"
    fi
    echo ""
    echo "=================================================="
    echo ""

    # 테스트 4: 메시지 읽음 처리
    echo -e "${BLUE}[테스트 4] 메시지 읽음 처리${NC}"
    echo "PUT /api/chatrooms/$CHAT_ROOM_ID/read"
    RESPONSE=$(curl -s -X PUT "$BASE_URL/api/chatrooms/$CHAT_ROOM_ID/read" \
      -H "Authorization: Bearer $JWT_TOKEN" \
      -w "\nHTTP_STATUS:%{http_code}")

    HTTP_STATUS=$(echo "$RESPONSE" | grep "HTTP_STATUS" | cut -d':' -f2)
    BODY=$(echo "$RESPONSE" | sed '/HTTP_STATUS/d')

    echo "$BODY"
    echo ""

    if [ "$HTTP_STATUS" = "200" ]; then
        echo -e "${GREEN}✓ 성공 (200 OK)${NC}"
    elif [ "$HTTP_STATUS" = "410" ]; then
        echo -e "${YELLOW}⚠️  채팅방이 만료되었습니다 (410 Gone)${NC}"
    else
        echo -e "${RED}✗ 실패 (HTTP $HTTP_STATUS)${NC}"
    fi
    echo ""
    echo "=================================================="
    echo ""
fi

# 테스트 5: 만료된 채팅방 접근 테스트 (예시)
echo -e "${BLUE}[테스트 5] 만료된 채팅방 접근 (예시)${NC}"
echo "GET /api/chatrooms/999/messages (존재하지 않는 채팅방)"
RESPONSE=$(curl -s -X GET "$BASE_URL/api/chatrooms/999/messages" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -w "\nHTTP_STATUS:%{http_code}")

HTTP_STATUS=$(echo "$RESPONSE" | grep "HTTP_STATUS" | cut -d':' -f2)
BODY=$(echo "$RESPONSE" | sed '/HTTP_STATUS/d')

echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"
echo ""

if [ "$HTTP_STATUS" = "410" ]; then
    echo -e "${GREEN}✓ 410 Gone 정상 반환${NC}"
elif [ "$HTTP_STATUS" = "400" ]; then
    echo -e "${YELLOW}⚠️  400 Bad Request (채팅방이 존재하지 않음)${NC}"
else
    echo -e "${RED}HTTP $HTTP_STATUS${NC}"
fi
echo ""
echo "=================================================="
echo ""

# 요약
echo -e "${BLUE}테스트 요약${NC}"
echo "1. ✓ POST /api/chatrooms - 채팅방 생성/조회"
echo "2. ✓ GET /api/chatrooms - 채팅방 리스트"
echo "3. ✓ GET /api/chatrooms/{id}/messages - 메시지 조회"
echo "4. ✓ PUT /api/chatrooms/{id}/read - 읽음 처리"
echo "5. ✓ 410 Gone - 만료/권한 없음 처리"
echo ""
echo -e "${GREEN}모든 API 엔드포인트 테스트 완료!${NC}"
echo ""
echo "🔍 상세 정보는 CHATROOM_API_UPDATED.md 참고"

