#!/bin/bash

# 마지막 선택(Final Choice) API 테스트 스크립트

BASE_URL="http://localhost:8080"
TOKEN=""

# 색상 코드
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}마지막 선택(Final Choice) API 테스트${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# JWT 토큰 입력
echo -e "${YELLOW}JWT 토큰을 입력하세요:${NC}"
read -r TOKEN

if [ -z "$TOKEN" ]; then
    echo -e "${RED}❌ 토큰이 입력되지 않았습니다.${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}✅ 토큰이 설정되었습니다.${NC}"
echo ""

# 1. 마지막 선택 옵션 조회
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}1. 마지막 선택 옵션 조회${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "${YELLOW}GET $BASE_URL/api/chat/final-choice/options${NC}"
echo ""

RESPONSE=$(curl -s -X GET "$BASE_URL/api/chat/final-choice/options" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json")

echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"

echo ""
echo -e "${GREEN}현재 매칭 상대와 채팅했던 다른 사람들의 목록이 표시됩니다.${NC}"
echo ""

# 선택할 프로필 ID 입력 받기
echo -e "${YELLOW}========================================${NC}"
echo -e "${YELLOW}선택할 프로필 ID를 입력하세요:${NC}"
echo -e "${YELLOW}(현재 매칭 상대 ID를 입력하면 그대로 유지)${NC}"
echo -e "${YELLOW}(다른 프로필 ID를 입력하면 매칭 변경)${NC}"
read -r SELECTED_PROFILE_ID

if [ -z "$SELECTED_PROFILE_ID" ]; then
    echo -e "${RED}❌ 프로필 ID가 입력되지 않았습니다.${NC}"
    exit 1
fi

echo ""

# 2. 마지막 선택 실행
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}2. 마지막 선택 실행${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "${YELLOW}POST $BASE_URL/api/chat/final-choice${NC}"
echo -e "${YELLOW}선택한 프로필 ID: $SELECTED_PROFILE_ID${NC}"
echo ""

REQUEST_BODY=$(cat <<EOF
{
  "selectedProfileId": $SELECTED_PROFILE_ID
}
EOF
)

echo "Request Body:"
echo "$REQUEST_BODY" | jq '.' 2>/dev/null || echo "$REQUEST_BODY"
echo ""

RESPONSE=$(curl -s -X POST "$BASE_URL/api/chat/final-choice" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "$REQUEST_BODY")

echo "Response:"
echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"

echo ""

# 결과 확인
IS_CHANGED=$(echo "$RESPONSE" | jq -r '.isChanged' 2>/dev/null)

if [ "$IS_CHANGED" = "true" ]; then
    echo -e "${GREEN}✅ 매칭이 변경되었습니다!${NC}"
    PARTNER_NICKNAME=$(echo "$RESPONSE" | jq -r '.partnerNickname' 2>/dev/null)
    PARTNER_MBTI=$(echo "$RESPONSE" | jq -r '.partnerMbti' 2>/dev/null)
    echo -e "${GREEN}새로운 매칭 상대: $PARTNER_NICKNAME ($PARTNER_MBTI)${NC}"
elif [ "$IS_CHANGED" = "false" ]; then
    echo -e "${GREEN}✅ 현재 매칭 상대를 유지합니다.${NC}"
    PARTNER_NICKNAME=$(echo "$RESPONSE" | jq -r '.partnerNickname' 2>/dev/null)
    PARTNER_MBTI=$(echo "$RESPONSE" | jq -r '.partnerMbti' 2>/dev/null)
    echo -e "${GREEN}매칭 상대: $PARTNER_NICKNAME ($PARTNER_MBTI)${NC}"
else
    echo -e "${RED}❌ 오류가 발생했습니다.${NC}"
fi

echo ""

# 3. 변경 후 채팅 목록 확인
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}3. 변경 후 채팅 목록 확인${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "${YELLOW}GET $BASE_URL/api/chat/list${NC}"
echo ""

RESPONSE=$(curl -s -X GET "$BASE_URL/api/chat/list" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json")

echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"

echo ""
echo -e "${GREEN}✅ 테스트 완료!${NC}"
echo -e "${BLUE}========================================${NC}"

