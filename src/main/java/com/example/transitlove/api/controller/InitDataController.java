package com.example.transitlove.api.controller;

import com.example.transitlove.domain.entity.BalanceGame;
import com.example.transitlove.domain.entity.Keyword;
import com.example.transitlove.domain.entity.Role;
import com.example.transitlove.domain.entity.User;
import com.example.transitlove.domain.repository.BalanceGameRepository;
import com.example.transitlove.domain.repository.KeywordRepository;
import com.example.transitlove.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;

@RestController
@RequestMapping("/api/init")
@RequiredArgsConstructor
public class InitDataController {

    private final UserRepository userRepository;
    private final KeywordRepository keywordRepository;
    private final BalanceGameRepository balanceGameRepository;

    @PostMapping("/data")
    public ResponseEntity<String> initData() {
        // 1. 테스트 사용자 추가
        if (!userRepository.existsById(1L)) {
            User testUser = User.builder()
                    .schoolEmail("test@school.com")
                    .name("테스트사용자")
                    .role(Role.USER)
                    .build();
            userRepository.save(testUser);
        }

        // 2. 키워드 추가
        if (keywordRepository.count() == 0) {
            keywordRepository.saveAll(Arrays.asList(
                    Keyword.builder().name("활발한").build(),
                    Keyword.builder().name("친절한").build(),
                    Keyword.builder().name("유머러스한").build(),
                    Keyword.builder().name("진지한").build(),
                    Keyword.builder().name("사교적인").build()
            ));
        }

        // 3. 밸런스 게임 추가
        if (balanceGameRepository.count() == 0) {
            balanceGameRepository.saveAll(Arrays.asList(
                    BalanceGame.builder().question("강아지 vs 고양이").option1("강아지").option2("고양이").build(),
                    BalanceGame.builder().question("아침형 vs 저녁형").option1("아침형 인간").option2("저녁형 인간").build(),
                    BalanceGame.builder().question("여행 vs 집").option1("여행 가기").option2("집에서 쉬기").build(),
                    BalanceGame.builder().question("영화 vs 독서").option1("영화 보기").option2("독서하기").build(),
                    BalanceGame.builder().question("커피 vs 차").option1("커피").option2("차").build()
            ));
        }

        return ResponseEntity.ok("테스트 데이터가 추가되었습니다.");
    }
}