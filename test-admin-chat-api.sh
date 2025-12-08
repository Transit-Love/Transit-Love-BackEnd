#!/bin/bash

# Admin Chat API 테스트 스크립트
# 사용법: ./test-admin-chat-api.sh {ADMIN_JWT_TOKEN}

# 색상 정의
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# API Base URL
BASE_URL="http://localhost:8080"

# JWT 토큰 확인
if [ -z "$1" ]; then
    echo -e "${RED}❌ JWT 토큰을 입력하세요${NC}"
    echo "사용법: ./test-admin-chat-api.sh {ADMIN_JWT_TOKEN}"
    exit 1
fi

JWT_TOKEN=$1

echo -e "${BLUE}======================================${NC}"
echo -e "${BLUE}  Admin Chat API 테스트 시작${NC}"
echo -e "${BLUE}======================================${NC}"
echo ""

# 1. 모든 활성 매칭 조회
echo -e "${GREEN}[1] 모든 활성 매칭 조회${NC}"
echo "GET $BASE_URL/api/admin/chat/matches"
echo ""
curl -X GET "$BASE_URL/api/admin/chat/matches" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -w "\n\nHTTP Status: %{http_code}\n" \
  -s | jq '.'
echo ""
echo "---"
echo ""

# 2. 비활성 매칭 포함 전체 조회
echo -e "${GREEN}[2] 비활성 매칭 포함 전체 조회${NC}"
echo "GET $BASE_URL/api/admin/chat/matches?includeInactive=true"
echo ""
curl -X GET "$BASE_URL/api/admin/chat/matches?includeInactive=true" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -w "\n\nHTTP Status: %{http_code}\n" \
  -s | jq '.'
echo ""
echo "---"
echo ""

# 3. 특정 매칭의 메시지 조회 (matchId=1)
echo -e "${GREEN}[3] 특정 매칭의 메시지 조회 (matchId=1)${NC}"
echo "GET $BASE_URL/api/admin/chat/matches/1/messages"
echo ""
curl -X GET "$BASE_URL/api/admin/chat/matches/1/messages" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -w "\n\nHTTP Status: %{http_code}\n" \
  -s | jq '.'
echo ""
echo "---"
echo ""

# 4. 존재하지 않는 매칭 조회 (에러 테스트)
echo -e "${GREEN}[4] 존재하지 않는 매칭 조회 (에러 테스트)${NC}"
echo "GET $BASE_URL/api/admin/chat/matches/99999/messages"
echo ""
curl -X GET "$BASE_URL/api/admin/chat/matches/99999/messages" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -w "\n\nHTTP Status: %{http_code}\n" \
  -s
echo ""
echo "---"
echo ""

echo -e "${BLUE}======================================${NC}"
echo -e "${BLUE}  테스트 완료${NC}"
echo -e "${BLUE}======================================${NC}"
echo ""
echo -e "${GREEN}✅ 모든 Admin Chat API 테스트가 완료되었습니다${NC}"
echo ""
echo "참고:"
echo "  - 모든 API는 ADMIN 권한이 필요합니다"
echo "  - JWT 토큰에 ROLE_ADMIN이 포함되어야 합니다"
echo "  - 조회만 가능하며 메시지 전송은 불가능합니다"
echo ""

