package kr.or.oti.project.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import kr.or.oti.project.domain.User;
import kr.or.oti.project.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String user_id) throws UsernameNotFoundException {
        log.debug("일반 로그인 인증 시도 - user_id={}", user_id);
        User user = userMapper.selectUserById(user_id);

        if (user == null) {
            log.warn("일반 로그인 실패 (사용자 미존재) - user_id={}", user_id);
            throw new UsernameNotFoundException("존재하지 않는 아이디입니다: " + user_id);
        }

        log.info("일반 로그인 성공 - user_id={}, user_no={}, role={}", user.getUser_id(), user.getUser_no(), user.getRole());
        return new CustomUserDetails(user);
    }
}