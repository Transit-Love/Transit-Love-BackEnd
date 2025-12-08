package com.example.transitlove.config;

import com.example.transitlove.domain.entity.HeartMessage;
import com.example.transitlove.domain.entity.Profile;
import com.example.transitlove.domain.repository.HeartMessageRepository;
import com.example.transitlove.domain.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ProfileRepository profileRepository;
    private final HeartMessageRepository heartMessageRepository;

    @Override
    public void run(String... args) {
        // 이미 데이터가 있으면 스킵
        if (heartMessageRepository.count() > 0) {
            log.info("HeartMessage 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        // Profile이 최소 2개 이상 있는지 확인
        List<Profile> profiles = profileRepository.findAll();
        if (profiles.size() < 2) {
            log.warn("Profile이 2개 미만입니다. HeartMessage 초기 데이터를 생성할 수 없습니다.");
            return;
        }

        // 첫 번째 Profile에서 두 번째 Profile로 속마음문자 전송
        Profile sender = profiles.get(0);
        Profile receiver = profiles.get(1);

        HeartMessage heartMessage = HeartMessage.builder()
                .senderProfile(sender)
                .receiverProfile(receiver)
                .content("첫 만남이 기억에 남아요 😊")
                .build();

        heartMessageRepository.save(heartMessage);
        log.info("HeartMessage 초기 데이터가 생성되었습니다: {} -> {}", sender.getNickname(), receiver.getNickname());
    }
}