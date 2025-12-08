#!/bin/bash

echo "=========================================="
echo "Transit Love 서버 시작"
echo "=========================================="

# JAVA_HOME 설정
export JAVA_HOME=/Users/hanjiwon/Library/Java/JavaVirtualMachines/corretto-17.0.16/Contents/Home

# 기존 프로세스 종료
echo "기존 서버 프로세스 종료 중..."
lsof -ti:8080 | xargs kill -9 2>/dev/null || echo "종료할 프로세스 없음"

# 서버 디렉토리로 이동
cd /Users/hanjiwon/Desktop/transitLove/BackEnd/transitLove

# 서버 시작
echo ""
echo "서버 시작 중... (포트 8080)"
echo "로그: server.log"
echo ""
echo "=========================================="
echo "주요 로그 확인 명령어:"
echo "  tail -f server.log | grep OAuth2"
echo "  tail -f server.log | grep JWT"
echo "=========================================="
echo ""

./gradlew bootRun

