#!/bin/bash

# 로그인 시 자동 매칭 기능 테스트 스크립트

echo "======================================"
echo "로그인 자동 매칭 기능 테스트"
echo "======================================"
echo ""

# 서버 로그 파일 경로
LOG_FILE="server.log"

echo "📋 테스트 시나리오:"
echo "1. OAuth2 로그인 수행"
echo "2. 서버 로그에서 자동 매칭 관련 로그 확인"
echo ""

echo "🔍 서버 로그 모니터링 시작..."
echo ""

# 로그인 자동 매칭 관련 로그 실시간 모니터링
if [ -f "$LOG_FILE" ]; then
    echo "✅ 로그 파일 발견: $LOG_FILE"
    echo ""
    echo "=== 최근 로그인 자동 매칭 로그 ==="
    tail -100 "$LOG_FILE" | grep -i "로그인 자동 매칭" || echo "아직 로그인 자동 매칭 로그가 없습니다."
    echo ""
    echo "=== 최근 매칭 관련 로그 ==="
    tail -100 "$LOG_FILE" | grep -i "매칭" | tail -20 || echo "매칭 관련 로그가 없습니다."
    echo ""
    echo "📊 실시간 로그 모니터링 (Ctrl+C로 종료):"
    echo ""
    tail -f "$LOG_FILE" | grep --line-buffered -i "로그인\|매칭"
else
    echo "⚠️  로그 파일을 찾을 수 없습니다: $LOG_FILE"
    echo ""
    echo "서버 로그를 확인하려면 서버가 실행 중이어야 합니다."
    echo ""
    echo "서버 시작 명령:"
    echo "  ./gradlew bootRun > server.log 2>&1 &"
    echo ""
    echo "또는 IDE에서 애플리케이션을 실행하세요."
fi

