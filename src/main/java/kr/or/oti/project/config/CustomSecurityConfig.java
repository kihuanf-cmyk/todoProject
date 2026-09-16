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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
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
            .logout(logout -> logout
                .logoutUrl("/user/logout")
                .logoutSuccessUrl("/user/login")
                .invalidateHttpSession(true)
            );

        return http.build();
    }

    // 3. 정적 자원은 보안 필터 제외
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }
}