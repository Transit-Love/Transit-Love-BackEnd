#!/bin/bash

# 🎤 음성 채팅 기능 테스트 스크립트

echo "================================================"
echo "🎤 음성 채팅 API 테스트"
echo "================================================"
echo ""

# JWT 토큰 설정
read -p "JWT 토큰을 입력하세요 (Bearer 제외): " TOKEN

if [ -z "$TOKEN" ]; then
    echo "❌ 토큰이 입력되지 않았습니다."
    exit 1
fi

BASE_URL="http://localhost:8080"
AUTH_HEADER="Authorization: Bearer $TOKEN"

echo ""
echo "================================================"
echo "1️⃣ 음성 채팅방 정보 조회 테스트"
echo "================================================"
read -p "채팅방 ID를 입력하세요: " CHAT_ROOM_ID

echo ""
echo "📡 요청 중..."
curl -X GET "$BASE_URL/api/voice/room/$CHAT_ROOM_ID" \
  -H "$AUTH_HEADER" \
  -H "Content-Type: application/json" \
  -w "\n\n✅ 상태 코드: %{http_code}\n" \
  -s | jq '.' 2>/dev/null || curl -X GET "$BASE_URL/api/voice/room/$CHAT_ROOM_ID" \
  -H "$AUTH_HEADER" \
  -H "Content-Type: application/json" \
  -w "\n\n✅ 상태 코드: %{http_code}\n" \
  -s

echo ""
echo "================================================"
echo "2️⃣ 활성 세션 상태 조회 테스트"
echo "================================================"

echo ""
echo "📡 요청 중..."
curl -X GET "$BASE_URL/api/voice/room/$CHAT_ROOM_ID/status" \
  -H "$AUTH_HEADER" \
  -H "Content-Type: application/json" \
  -w "\n\n✅ 상태 코드: %{http_code}\n" \
  -s | jq '.' 2>/dev/null || curl -X GET "$BASE_URL/api/voice/room/$CHAT_ROOM_ID/status" \
  -H "$AUTH_HEADER" \
  -H "Content-Type: application/json" \
  -w "\n\n✅ 상태 코드: %{http_code}\n" \
  -s

echo ""
echo "================================================"
echo "3️⃣ 음성 통화 강제 종료 테스트"
echo "================================================"
read -p "강제 종료를 테스트하시겠습니까? (y/n): " CONFIRM

if [ "$CONFIRM" = "y" ] || [ "$CONFIRM" = "Y" ]; then
    echo ""
    echo "📡 요청 중..."
    curl -X POST "$BASE_URL/api/voice/room/$CHAT_ROOM_ID/end" \
      -H "$AUTH_HEADER" \
      -H "Content-Type: application/json" \
      -w "\n\n✅ 상태 코드: %{http_code}\n" \
      -s

    echo ""
    echo "✅ 강제 종료 요청 완료"
else
    echo "⏭️  강제 종료 테스트를 건너뜁니다."
fi

echo ""
echo "================================================"
echo "🎉 테스트 완료!"
echo "================================================"
echo ""
echo "📋 다음 단계:"
echo "1. 테스트 페이지 열기: http://localhost:8080/voice-chat-test.html"
echo "2. 2개의 브라우저 탭에서 WebSocket 연결"
echo "3. 음성 통화 시작 및 WebRTC 테스트"
echo ""
echo "📚 자세한 내용:"
echo "- API 문서: VOICE_CHAT_API_README.md"
echo "- 빠른 시작: VOICE_CHAT_QUICK_START.md"
echo "- 구현 상세: VOICE_CHAT_IMPLEMENTATION.md"
echo ""

