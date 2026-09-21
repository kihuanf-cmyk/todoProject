package kr.or.oti.project.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import kr.or.oti.project.domain.User;
import kr.or.oti.project.domain.UserRole;
import kr.or.oti.project.mapper.UserMapper;
import kr.or.oti.project.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//수정 (login 삭제, role 기본값 추가)
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;

	@Override
	public boolean joinUser(User user) {
		log.debug("회원가입 중복 확인 - user_id={}", user.getUser_id());
		int count = userMapper.countUserById(user.getUser_id());
		if (count > 0) {
			log.warn("회원가입 거부 - 이미 존재하는 user_id={}", user.getUser_id());
			return false;
		}
		String encodedPwd = passwordEncoder.encode(user.getPwd());
		user.setPwd(encodedPwd);
		user.setRole(UserRole.USER); // 회원가입 시 기본 권한 부여
		userMapper.insertUser(user);
		log.info("회원 저장 완료 - user_id={}, role={}", user.getUser_id(), user.getRole());
		return true;
	}
}
