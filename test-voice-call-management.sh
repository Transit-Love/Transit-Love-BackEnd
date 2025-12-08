#!/bin/bash

# 음성 통화 관리 API 테스트 스크립트

BASE_URL="http://localhost:8080"
TOKEN="your_jwt_token_here"  # JWT 토큰을 여기에 입력하세요

echo "=========================================="
echo "음성 통화 관리 API 테스트"
echo "=========================================="

# 색상 코드
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. 음소거 상태 변경
echo -e "\n${YELLOW}1. 음소거 상태 변경${NC}"
curl -X POST "${BASE_URL}/api/voice/room/1/mute" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "profileId": 1,
    "isMuted": true,
    "timestamp": '$(date +%s000)'
  }' | jq '.'

# 2. 음소거 상태 조회
echo -e "\n${YELLOW}2. 음소거 상태 조회${NC}"
curl -X GET "${BASE_URL}/api/voice/room/1/mute/1" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.'

# 3. 통화 기록 저장
echo -e "\n${YELLOW}3. 통화 기록 저장${NC}"
curl -X POST "${BASE_URL}/api/voice/call/history" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "chatRoomId": 1,
    "callerProfileId": 1,
    "receiverProfileId": 2,
    "startTime": "'$(date -u -v-15M +"%Y-%m-%dT%H:%M:%S")'",
    "endTime": "'$(date -u +"%Y-%m-%dT%H:%M:%S")'",
    "duration": 900,
    "callStatus": "COMPLETED",
    "voiceModulationUsed": true
  }' | jq '.'

# 4. 채팅방별 통화 기록 조회
echo -e "\n${YELLOW}4. 채팅방별 통화 기록 조회${NC}"
curl -X GET "${BASE_URL}/api/voice/call/history?chatRoomId=1&page=0&size=20" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.'

# 5. 내 통화 기록 조회
echo -e "\n${YELLOW}5. 내 통화 기록 조회${NC}"
curl -X GET "${BASE_URL}/api/voice/call/history/my?page=0&size=20" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.'

# 6. 통화 통계 조회
echo -e "\n${YELLOW}6. 통화 통계 조회${NC}"
curl -X GET "${BASE_URL}/api/voice/call/statistics?chatRoomId=1" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.'

echo -e "\n${GREEN}=========================================="
echo "테스트 완료"
echo -e "==========================================${NC}"

