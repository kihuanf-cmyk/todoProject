package kr.or.oti.project.security;

import java.util.Map;

import org.springframework.security.oauth2.core.user.OAuth2User;

import kr.or.oti.project.domain.User;

// CustomUserDetails를 상속 -> getUser_no(), getUsername() 등 기존 기능 그대로 사용
// OAuth2User도 구현 -> Spring Security의 OAuth2 로그인 처리 흐름과 호환
public class CustomOAuth2UserDetails extends CustomUserDetails implements OAuth2User {

    private final Map<String, Object> attributes; // 구글이 내려준 원본 사용자 정보(email, name 등)

    public CustomOAuth2UserDetails(User user, Map<String, Object> attributes) {
        super(user);
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        // OAuth2User가 요구하는 식별자 -> user_id(이메일)로 사용
        return getUsername();
    }
}