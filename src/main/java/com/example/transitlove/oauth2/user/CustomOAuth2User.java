package com.example.transitlove.oauth2.user;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class CustomOAuth2User implements OAuth2User {

    private Long id;
    private String email;
    private String role;
    private Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    public CustomOAuth2User(Long id, String email, String role, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.authorities = authorities;
    }

    public static CustomOAuth2User create(Long id, String email, String role) {
        return new CustomOAuth2User(id, email, role, Collections.emptyList());
    }

    public static CustomOAuth2User create(Long id, String email, String role, Map<String, Object> attributes) {
        CustomOAuth2User customOAuth2User = CustomOAuth2User.create(id, email, role);
        customOAuth2User.setAttributes(attributes);
        return customOAuth2User;
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}