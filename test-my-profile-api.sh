#!/bin/bash

# 내 프로필 조회 API 테스트 스크립트
# Usage: ./test-my-profile-api.sh [JWT_TOKEN]

# 색상 정의
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# API 엔드포인트
BASE_URL="http://localhost:8080"
PROFILE_ME_URL="${BASE_URL}/api/profiles/me"

# JWT 토큰 (인자로 받거나 환경변수 사용)
JWT_TOKEN="${1:-$JWT_TOKEN}"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  내 프로필 조회 API 테스트${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 토큰 확인
if [ -z "$JWT_TOKEN" ]; then
    echo -e "${RED}❌ JWT 토큰이 제공되지 않았습니다.${NC}"
    echo "사용법: ./test-my-profile-api.sh YOUR_JWT_TOKEN"
    echo "또는 환경변수 설정: export JWT_TOKEN=YOUR_JWT_TOKEN"
    exit 1
fi

echo -e "${YELLOW}📝 사용 중인 토큰: ${JWT_TOKEN:0:20}...${NC}"
echo ""

# 테스트 1: 내 프로필 조회
echo -e "${BLUE}[테스트 1] GET /api/profiles/me - 내 프로필 조회${NC}"
echo "----------------------------------------"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "${PROFILE_ME_URL}" \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -H "Content-Type: application/json")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    echo -e "${GREEN}✅ 성공 (HTTP $HTTP_CODE)${NC}"
    echo ""
    echo -e "${YELLOW}응답 데이터:${NC}"
    echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"

    # 프로필 정보 파싱
    NICKNAME=$(echo "$BODY" | jq -r '.nickname' 2>/dev/null)
    MBTI=$(echo "$BODY" | jq -r '.mbti' 2>/dev/null)
    KEYWORDS=$(echo "$BODY" | jq -r '.keywords | length' 2>/dev/null)
    BALANCE_GAMES=$(echo "$BODY" | jq -r '.balanceGameAnswers | length' 2>/dev/null)

    echo ""
    echo -e "${GREEN}📊 프로필 요약:${NC}"
    echo "  • 닉네임: $NICKNAME"
    echo "  • MBTI: $MBTI"
    echo "  • 관심사 키워드 수: $KEYWORDS"
    echo "  • 밸런스 게임 답변 수: $BALANCE_GAMES"

    # 키워드 목록 출력
    if [ "$KEYWORDS" != "null" ] && [ "$KEYWORDS" -gt 0 ]; then
        echo ""
        echo -e "${YELLOW}  관심사 키워드:${NC}"
        echo "$BODY" | jq -r '.keywords[] | "    - \(.name)"' 2>/dev/null
    fi

    # 밸런스 게임 답변 출력
    if [ "$BALANCE_GAMES" != "null" ] && [ "$BALANCE_GAMES" -gt 0 ]; then
        echo ""
        echo -e "${YELLOW}  밸런스 게임 답변:${NC}"
        echo "$BODY" | jq -r '.balanceGameAnswers[] | "    Q: \(.question)\n    A: \(if .selectedOption == 1 then .option1 else .option2 end)"' 2>/dev/null
    fi

elif [ "$HTTP_CODE" -eq 404 ]; then
    echo -e "${YELLOW}⚠️  프로필 없음 (HTTP $HTTP_CODE)${NC}"
    echo "프로필이 생성되지 않았습니다. 먼저 프로필을 생성해주세요."
    echo ""
    echo -e "${YELLOW}응답:${NC}"
    echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"
elif [ "$HTTP_CODE" -eq 401 ]; then
    echo -e "${RED}❌ 인증 실패 (HTTP $HTTP_CODE)${NC}"
    echo "JWT 토큰이 유효하지 않습니다."
    echo ""
    echo -e "${YELLOW}응답:${NC}"
    echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"
else
    echo -e "${RED}❌ 실패 (HTTP $HTTP_CODE)${NC}"
    echo ""
    echo -e "${YELLOW}응답:${NC}"
    echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"
fi

echo ""
echo "----------------------------------------"

# 테스트 2: 잘못된 토큰으로 요청 (보안 테스트)
echo ""
echo -e "${BLUE}[테스트 2] 인증 없이 요청 (보안 테스트)${NC}"
echo "----------------------------------------"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "${PROFILE_ME_URL}" \
  -H "Content-Type: application/json")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)

if [ "$HTTP_CODE" -eq 401 ] || [ "$HTTP_CODE" -eq 403 ]; then
    echo -e "${GREEN}�� 정상 (HTTP $HTTP_CODE) - 인증 없이 접근 차단됨${NC}"
else
    echo -e "${RED}⚠️  예상치 못한 응답 (HTTP $HTTP_CODE)${NC}"
    BODY=$(echo "$RESPONSE" | sed '$d')
    echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"
fi

echo ""
echo "----------------------------------------"

# 최종 요약
echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  테스트 완료${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${GREEN}✅ 내 프로필 조회 API 테스트 완료!${NC}"
echo ""
echo "다음 단계:"
echo "  1. 프로필이 없다면: POST /api/profiles 로 프로필 생성"
echo "  2. 프로필 수정: PUT /api/profiles/me"
echo "  3. 매칭된 프로필 보기: GET /api/profiles/matched"
echo "  4. 매칭 가능한 프로필 보기: GET /api/profiles/unmatched"
echo ""

