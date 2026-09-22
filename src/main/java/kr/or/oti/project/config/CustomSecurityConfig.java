package kr.or.oti.project.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import kr.or.oti.project.security.CustomOAuth2UserService;
import kr.or.oti.project.security.CustomUserDetailsService;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class CustomSecurityConfig {

	// 1. 비밀번호 암호화 빈 등록
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// 2. 보안 필터 체인 설정 (Spring Security 5.7.x 기준 antMatchers 사용)
	@Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           CustomUserDetailsService userDetailsService, CustomOAuth2UserService oAuth2UserService) throws Exception {
        // @Bean 메서드의 파라미터는 스프링이 자동으로 주입해줍니다.
        // 필드 선언 + 생성자를 만들 필요가 없어 간단합니다.     
		http
            .authorizeRequests(auth -> auth
                .antMatchers("/admin/**").hasRole("ADMIN")            // 관리자 전용
                .antMatchers("/todo/**").hasAnyRole("USER", "ADMIN")  // 로그인 회원 전용
                .anyRequest().permitAll()                             // 나머지(회원가입/로그인 등)는 누구나 접근
            )
            .formLogin(form -> form
                .loginPage("/user/login")              // 커스텀 로그인 화면
                .usernameParameter("user_id")          // 폼 필드명 매칭 (기본값 username → user_id)
                .passwordParameter("pwd")              // 폼 필드명 매칭 (기본값 password → pwd)
                .defaultSuccessUrl("/todo/list", true) // 로그인 성공 시 이동
                .permitAll()
            )
            
            // 구글 OAuth2 로그인 설정
            .oauth2Login(oauth2 -> oauth2
            		.loginPage("/user/login") //로그인 화면은 폼 로그인과 공유
            		.defaultSuccessUrl("/todo/list", true) // 성공 시 이동 경로도 동일하게
            		.userInfoEndpoint(userInfo -> userInfo
            				.userService(oAuth2UserService) //2)에서 파라미터로 받은 빈 사용
            				)
            		)
            
            // ✅ 자동 로그인(remember-me) 설정
            .rememberMe(remember -> remember
                .key("dayplanner-remember-key")             // 쿠키 토큰 서명에 쓰이는 고유 키 (바뀌면 기존 쿠키 전부 무효)
                .rememberMeParameter("remember-me")         // 로그인 폼 체크박스의 name 값
                .tokenValiditySeconds(60 * 60 * 24 * 7)     // 유지 기간 7일 (초 단위)
                .userDetailsService(userDetailsService)     // 쿠키로 재인증할 때 회원 정보를 다시 조회
            )
            .logout(logout -> logout
                .logoutUrl("/user/logout")
                .logoutSuccessUrl("/user/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me") // ✅ 로그아웃 시 자동로그인 쿠키까지 삭제
            )
            // 관리자 페이지 등 권한 없는 URL 무단 접근 시 403 에러 안내 페이지로 이동
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/user/denied")
            );

        return http.build();
    }

	// 3. 정적 자원은 보안 필터 제외
	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
	}
}