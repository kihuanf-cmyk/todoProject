package kr.or.oti.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import kr.or.oti.project.mapper.UserMapper;

@RestController  // 화면(html) 없이 문자열을 그대로 브라우저에 출력
public class TestController {

    @Autowired
    private DataSource dataSource;  // application.properties의 DB 접속 정보로 스프링이 자동 구성해줌

    @Autowired
    private UserMapper userMapper;  // MyBatis가 실제로 이 인터페이스를 스캔했는지 확인용
    
    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }

    @GetMapping("/db-test")
    public String dbTest() {
        try (Connection conn = dataSource.getConnection()) {
            boolean closed = conn.isClosed();  // 연결 직후라 반드시 false여야 함
            return "DB 연결 성공! isClosed() = " + closed;
        } catch (Exception e) {
            return "DB 연결 실패: " + e.getMessage();
        }
    }

    @GetMapping("/mapper-test")
    public String mapperTest() {
        int count = userMapper.countUserById("test@test.com");  // 존재하지 않는 아이디로 조회
        return "매퍼 정상 동작! count = " + count;  // 테이블이 비어있으면 0이 나오는 게 정상
    }

    // 관리자(ROLE_ADMIN) 전용 테스트 엔드포인트
    // 일반 회원(ROLE_USER)이 접속하면 403 차단되어 /user/denied 로 이동함
    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "관리자 전용 대시보드입니다. 환영합니다!";
    }
}