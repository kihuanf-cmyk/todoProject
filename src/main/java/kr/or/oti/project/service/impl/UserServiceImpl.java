package kr.or.oti.project.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import kr.or.oti.project.domain.User;
import kr.or.oti.project.domain.UserRole;
import kr.or.oti.project.mapper.UserMapper;
import kr.or.oti.project.service.UserService;
import lombok.RequiredArgsConstructor;

//수정 (login 삭제, role 기본값 추가)
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;

	@Override
	public boolean joinUser(User user) {
		int count = userMapper.countUserById(user.getUser_id());
		if (count > 0) {
			return false;
		}
		String encodedPwd = passwordEncoder.encode(user.getPwd());
		user.setPwd(encodedPwd);
		user.setRole(UserRole.USER); // 회원가입 시 기본 권한 부여
		userMapper.insertUser(user);
		return true;
	}
}