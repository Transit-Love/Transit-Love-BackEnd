package com.example.transitlove.oauth2.service;

import com.example.transitlove.domain.entity.Role;
import com.example.transitlove.domain.entity.User;
import com.example.transitlove.domain.repository.UserRepository;
import com.example.transitlove.oauth2.user.CustomOAuth2User;
import com.example.transitlove.oauth2.user.GoogleOAuth2UserInfo;
import com.example.transitlove.oauth2.user.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    private static final List<String> ADMIN_EMAILS = Arrays.asList(
            "24.051@bssm.hs.kr",
            "24.058@bssm.hs.kr"
    );

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        OAuth2UserInfo oAuth2UserInfo = new GoogleOAuth2UserInfo(oAuth2User.getAttributes());

        if (oAuth2UserInfo.getEmail() == null || oAuth2UserInfo.getEmail().isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        if (!oAuth2UserInfo.getEmail().endsWith("@bssm.hs.kr")) {
            throw new OAuth2AuthenticationException("Only @bssm.hs.kr email accounts are allowed");
        }

        Optional<User> userOptional = userRepository.findBySchoolEmail(oAuth2UserInfo.getEmail());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            user = user.update(oAuth2UserInfo.getName());
            userRepository.save(user);
        } else {
            user = registerNewUser(oAuth2UserInfo);
        }

        return CustomOAuth2User.create(user.getId(), user.getSchoolEmail(), user.getRole().name(), oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserInfo oAuth2UserInfo) {
        Role role = ADMIN_EMAILS.contains(oAuth2UserInfo.getEmail()) ? Role.ADMIN : Role.USER;

        User user = User.builder()
                .name(oAuth2UserInfo.getName())
                .schoolEmail(oAuth2UserInfo.getEmail())
                .role(role)
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }
}