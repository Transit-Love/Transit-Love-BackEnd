package com.example.transitlove.service;

import com.example.transitlove.api.dto.HeartMessageRequest;
import com.example.transitlove.api.dto.HeartMessageResponse;
import com.example.transitlove.domain.entity.HeartMessage;
import com.example.transitlove.domain.entity.Profile;
import com.example.transitlove.domain.entity.User;
import com.example.transitlove.domain.repository.HeartMessageRepository;
import com.example.transitlove.domain.repository.ProfileRepository;
import com.example.transitlove.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeartMessageService {

    private final HeartMessageRepository heartMessageRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Transactional
    public HeartMessageResponse sendHeartMessage(Long userId, HeartMessageRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Profile senderProfile = profileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("발신자 프로필을 찾을 수 없습니다."));

        Profile receiverProfile = profileRepository.findById(request.getReceiverProfileId())
                .orElseThrow(() -> new IllegalArgumentException("수신자 프로필을 찾을 수 없습니다."));

        if (senderProfile.getId().equals(receiverProfile.getId())) {
            throw new IllegalArgumentException("자기 자신에게 속마음문자를 보낼 수 없습니다.");
        }

        HeartMessage heartMessage = HeartMessage.builder()
                .senderProfile(senderProfile)
                .receiverProfile(receiverProfile)
                .content(request.getContent())
                .build();

        HeartMessage savedMessage = heartMessageRepository.save(heartMessage);
        return HeartMessageResponse.from(savedMessage);
    }

    public List<HeartMessageResponse> getHeartMessages(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        List<HeartMessage> messages = heartMessageRepository.findByProfile(profile);

        return messages.stream()
                .map(HeartMessageResponse::from)
                .collect(Collectors.toList());
    }

    public List<HeartMessageResponse> getReceivedHeartMessages(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        List<HeartMessage> messages = heartMessageRepository.findByReceiverProfileOrderBySentAtDesc(profile);

        return messages.stream()
                .map(HeartMessageResponse::from)
                .collect(Collectors.toList());
    }

    public List<HeartMessageResponse> getSentHeartMessages(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        List<HeartMessage> messages = heartMessageRepository.findBySenderProfileOrderBySentAtDesc(profile);

        return messages.stream()
                .map(HeartMessageResponse::from)
                .collect(Collectors.toList());
    }
}