package todoProject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import kr.or.oti.project.domain.User;
import kr.or.oti.project.domain.UserRole;
import kr.or.oti.project.mapper.UserMapper;
import kr.or.oti.project.security.CustomOAuth2UserServiceImpl;
import kr.or.oti.project.service.impl.UserServiceImpl;

// 테스트 작성일: 2026-09-17
// 테스트 순서: 도메인 -> 서비스 -> DB/매퍼 -> 컨트롤러 -> DTO
@ExtendWith(MockitoExtension.class)
class UserTests {

	@Nested
	@DisplayName("User 도메인 단위 테스트")
	class User도메인테스트 {

		@Test
		@DisplayName("User 객체는 번호, 아이디, 이름, 권한을 입력한 값으로 보관한다")
		void User객체는입력한값을보관한다() {
			User user = new User();

			user.setUser_no(1L);
			user.setUser_id("user01");
			user.setPwd("plain-password");
			user.setUser_name("홍길동");
			user.setRole(UserRole.USER);

			assertThat(user.getUser_no()).isEqualTo(1L);
			assertThat(user.getUser_id()).isEqualTo("user01");
			assertThat(user.getPwd()).isEqualTo("plain-password");
			assertThat(user.getUser_name()).isEqualTo("홍길동");
			assertThat(user.getRole()).isEqualTo(UserRole.USER);
		}

		@Test
		@DisplayName("UserRole은 일반 사용자와 관리자를 구분한다")
		void UserRole은사용자와관리자를구분한다() {
			assertThat(UserRole.values()).containsExactly(UserRole.USER, UserRole.ADMIN);
		}
	}

	@Nested
	@DisplayName("User 서비스 통합 테스트")
	class User서비스테스트 {

		@Mock
		private UserMapper userMapper;

		@Mock
		private PasswordEncoder passwordEncoder;

		@InjectMocks
		private UserServiceImpl userService;

		@Test
		@DisplayName("새로운 회원은 비밀번호를 암호화하고 일반 사용자 권한으로 저장한다")
		void 새로운회원은암호화하고일반권한으로저장한다() {
			User user = new User();
			user.setUser_id("new-user");
			user.setPwd("plain-password");
			when(userMapper.countUserById("new-user")).thenReturn(0);
			when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");

			boolean joined = userService.joinUser(user);

			assertThat(joined).isTrue();
			assertThat(user.getPwd()).isEqualTo("encoded-password");
			assertThat(user.getRole()).isEqualTo(UserRole.USER);
			verify(userMapper).insertUser(user);
		}

		@Test
		@DisplayName("이미 존재하는 아이디는 회원가입을 거부하고 비밀번호를 암호화하지 않는다")
		void 이미존재하는아이디는회원가입을거부한다() {
			User user = new User();
			user.setUser_id("existing-user");
			user.setPwd("plain-password");
			when(userMapper.countUserById("existing-user")).thenReturn(1);

			boolean joined = userService.joinUser(user);

			assertThat(joined).isFalse();
			verify(passwordEncoder, never()).encode("plain-password");
			verify(userMapper, never()).insertUser(user);
		}
	}
	
	// UserTests.java 하단, User서비스테스트 Nested 클래스 뒤에 추가
	@Nested
	@DisplayName("구글 OAuth 로그인 사용자 판별 테스트")
	class GoogleOAuth사용자판별테스트 {

	    @Mock
	    private UserMapper userMapper;

	    @Mock
	    private PasswordEncoder passwordEncoder;

	    @InjectMocks
	    private CustomOAuth2UserServiceImpl customOAuth2UserServiceImpl;

	    @Test
	    @DisplayName("처음 로그인하는 이메일이면 신규 회원으로 자동 등록한다")
	    void 신규이메일은자동등록된다() {
	        when(userMapper.selectUserById("new@gmail.com")).thenReturn(null);
	        when(passwordEncoder.encode(org.mockito.ArgumentMatchers.anyString())).thenReturn("encoded-dummy-pwd");

	        User result = customOAuth2UserServiceImpl.resolveUser("new@gmail.com", "홍길동");

	        assertThat(result.getUser_id()).isEqualTo("new@gmail.com");
	        assertThat(result.getUser_name()).isEqualTo("홍길동");
	        assertThat(result.getRole()).isEqualTo(UserRole.USER);
	        assertThat(result.getPwd()).isEqualTo("encoded-dummy-pwd");
	        verify(userMapper).insertUser(result);
	    }

	    @Test
	    @DisplayName("이미 가입된 이메일이면 기존 회원 정보를 그대로 반환하고 재등록하지 않는다")
	    void 기존이메일은재등록하지않는다() {
	        User existingUser = new User();
	        existingUser.setUser_no(5L);
	        existingUser.setUser_id("existing@gmail.com");
	        existingUser.setUser_name("김철수");
	        existingUser.setRole(UserRole.USER);
	        when(userMapper.selectUserById("existing@gmail.com")).thenReturn(existingUser);

	        User result = customOAuth2UserServiceImpl.resolveUser("existing@gmail.com", "김철수");

	        assertThat(result).isSameAs(existingUser);
	        verify(userMapper, never()).insertUser(org.mockito.ArgumentMatchers.any());
	    }
	}
}
