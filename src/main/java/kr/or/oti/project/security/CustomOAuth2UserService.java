package kr.or.oti.project.security;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

// Spring Security가 제공하는 OAuth2UserService<Request, User> 인터페이스를 그대로 상속
// -> loadUser() 메서드 시그니처를 Spring 쪽에서 이미 정의해주므로 따로 선언할 필요 없음
public interface CustomOAuth2UserService extends OAuth2UserService<OAuth2UserRequest, OAuth2User> {
}