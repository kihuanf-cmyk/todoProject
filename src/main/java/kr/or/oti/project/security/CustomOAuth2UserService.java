package kr.or.oti.project.security;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import kr.or.oti.project.domain.User;
import kr.or.oti.project.domain.UserRole;
import kr.or.oti.project.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// DefaultOAuth2UserService: 구글에서 사용자 정보를 실제로 가져오는 기본 로직 재사용을 위해 상속
//수정 코드 - 인터페이스 없이 DefaultOAuth2UserService만 상속하는 단일 클래스로 변경
//DefaultOAuth2UserService: 구글에서 사용자 정보를 실제로 가져오는 기본 로직 재사용을 위해 상속
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
	
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

 // 수정 코드 - 회원 판별 로직을 resolveUser()로 분리 (테스트 가능하게)
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        log.debug("구글 로그인 요청 수신 - email={}, name={}", email, name);

        User loginUser = resolveUser(email, name); // 신규/기존 판별 로직은 별도 메서드로 분리

        return new CustomOAuth2UserDetails(loginUser, oAuth2User.getAttributes());
    }

    // package-private: 테스트 클래스에서 직접 호출해서 단위 테스트하기 위함
    public User resolveUser(String email, String name) {
        User existingUser = userMapper.selectUserById(email);

        if (existingUser == null) {
            User newUser = new User();
            newUser.setUser_id(email);
            newUser.setPwd(passwordEncoder.encode(UUID.randomUUID().toString()));
            newUser.setUser_name(name);
            newUser.setRole(UserRole.USER);
            userMapper.insertUser(newUser);
            log.info("구글 로그인 - 신규 사용자 자동 등록: email={}, user_no={}", email, newUser.getUser_no());
            return newUser;
        }

        log.debug("구글 로그인 - 기존 사용자 확인: email={}, user_no={}", email, existingUser.getUser_no());
        return existingUser;
    }
}