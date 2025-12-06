package com.example.transitlove.service;

import com.example.transitlove.api.dto.ProfileCreateRequest;
import com.example.transitlove.api.dto.ProfileResponse;
import com.example.transitlove.domain.entity.*;
import com.example.transitlove.domain.repository.BalanceGameRepository;
import com.example.transitlove.domain.repository.KeywordRepository;
import com.example.transitlove.domain.repository.ProfileRepository;
import com.example.transitlove.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final KeywordRepository keywordRepository;
    private final BalanceGameRepository balanceGameRepository;

    @Transactional
    public ProfileResponse createProfile(Long userId, ProfileCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (profileRepository.existsByUser(user)) {
            throw new IllegalStateException("이미 프로필이 존재합니다.");
        }

        List<Keyword> keywords = keywordRepository.findByIdIn(request.getKeywordIds());
        if (keywords.size() != request.getKeywordIds().size()) {
            throw new IllegalArgumentException("유효하지 않은 키워드 ID가 포함되어 있습니다.");
        }

        List<Long> balanceGameIds = request.getBalanceGameAnswers().stream()
                .map(ProfileCreateRequest.BalanceGameAnswerDto::getBalanceGameId)
                .collect(Collectors.toList());

        List<BalanceGame> balanceGames = balanceGameRepository.findByIdIn(balanceGameIds);
        if (balanceGames.size() != balanceGameIds.size()) {
            throw new IllegalArgumentException("유효하지 않은 밸런스 게임 ID가 포함되어 있습니다.");
        }

        Map<Long, BalanceGame> balanceGameMap = balanceGames.stream()
                .collect(Collectors.toMap(BalanceGame::getId, bg -> bg));

        Profile profile = Profile.builder()
                .user(user)
                .nickname(request.getNickname())
                .mbti(request.getMbti())
                .build();

        for (Keyword keyword : keywords) {
            ProfileKeyword profileKeyword = ProfileKeyword.builder()
                    .keyword(keyword)
                    .build();
            profile.addKeyword(profileKeyword);
        }

        for (ProfileCreateRequest.BalanceGameAnswerDto answerDto : request.getBalanceGameAnswers()) {
            if (answerDto.getSelectedOption() != 1 && answerDto.getSelectedOption() != 2) {
                throw new IllegalArgumentException("선택한 옵션은 1 또는 2여야 합니다.");
            }

            BalanceGame balanceGame = balanceGameMap.get(answerDto.getBalanceGameId());
            BalanceGameAnswer answer = BalanceGameAnswer.builder()
                    .balanceGame(balanceGame)
                    .selectedOption(answerDto.getSelectedOption())
                    .build();
            profile.addBalanceGameAnswer(answer);
        }

        Profile savedProfile = profileRepository.save(profile);
        return ProfileResponse.from(savedProfile);
    }

    public ProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        return ProfileResponse.from(profile);
    }

    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        List<Keyword> keywords = keywordRepository.findByIdIn(request.getKeywordIds());
        if (keywords.size() != request.getKeywordIds().size()) {
            throw new IllegalArgumentException("유효하지 않은 키워드 ID가 포함되어 있습니다.");
        }

        List<Long> balanceGameIds = request.getBalanceGameAnswers().stream()
                .map(ProfileCreateRequest.BalanceGameAnswerDto::getBalanceGameId)
                .collect(Collectors.toList());

        List<BalanceGame> balanceGames = balanceGameRepository.findByIdIn(balanceGameIds);
        if (balanceGames.size() != balanceGameIds.size()) {
            throw new IllegalArgumentException("유효하지 않은 밸런스 게임 ID가 포함되어 있습니다.");
        }

        Map<Long, BalanceGame> balanceGameMap = balanceGames.stream()
                .collect(Collectors.toMap(BalanceGame::getId, bg -> bg));

        profile.updateProfile(request.getNickname(), request.getMbti());

        profile.clearKeywords();
        for (Keyword keyword : keywords) {
            ProfileKeyword profileKeyword = ProfileKeyword.builder()
                    .keyword(keyword)
                    .build();
            profile.addKeyword(profileKeyword);
        }

        profile.clearBalanceGameAnswers();
        for (ProfileCreateRequest.BalanceGameAnswerDto answerDto : request.getBalanceGameAnswers()) {
            if (answerDto.getSelectedOption() != 1 && answerDto.getSelectedOption() != 2) {
                throw new IllegalArgumentException("선택한 옵션은 1 또는 2여야 합니다.");
            }

            BalanceGame balanceGame = balanceGameMap.get(answerDto.getBalanceGameId());
            BalanceGameAnswer answer = BalanceGameAnswer.builder()
                    .balanceGame(balanceGame)
                    .selectedOption(answerDto.getSelectedOption())
                    .build();
            profile.addBalanceGameAnswer(answer);
        }

        return ProfileResponse.from(profile);
    }
}