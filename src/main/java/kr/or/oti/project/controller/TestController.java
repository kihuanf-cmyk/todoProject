package kr.or.oti.project.controller;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.or.oti.project.dto.TodoResponseDto;
import kr.or.oti.project.dto.TodoStatsDTO;
import kr.or.oti.project.mapper.UserMapper;
import kr.or.oti.project.security.CustomUserDetails;
import kr.or.oti.project.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TestController {

    private final DataSource dataSource;
    private final UserMapper userMapper;
    private final TodoService todoService;

    @GetMapping("/hello")
    public String hello() {
        log.debug("[TestController] /hello 요청 수신");
        return "Hello World";
    }

    // 브라우저에서 자동화 테스트와 수동 확인 주소를 한 번에 확인하는 안내 화면
    @GetMapping("/test-guide")
    public Map<String, Object> testGuide() {
        log.debug("[TestController] /test-guide 요청 수신");
        Map<String, Object> guide = new HashMap<>();
        guide.put("automatedTests", List.of("TodoTests", "UserTests", "CalendarTests", "KanbanTests", "HomeTests", "GlobalExceptionHandlerTest"));
        guide.put("manualEndpoints", Map.of(
                "hello", "/hello",
                "dbCheck", "/db-test",
                "mapperCheck", "/mapper-test",
                "authInfo", "/test/auth-info",
                "todoStats", "/test/stats",
                "adminCheck", "/admin/dashboard"
        ));
        return guide;
    }

    @GetMapping("/db-test")
    public String dbTest() {
        log.debug("[TestController] /db-test DB 연결 테스트 수행");
        try (Connection conn = dataSource.getConnection()) {
            boolean closed = conn.isClosed();
            log.info("[TestController] DB 연결 성공 - isClosed()={}", closed);
            return "DB 연결 성공! isClosed() = " + closed;
        } catch (Exception e) {
            log.error("[TestController] DB 연결 실패", e);
            return "DB 연결 실패: " + e.getMessage();
        }
    }

    @GetMapping("/mapper-test")
    public String mapperTest() {
        log.debug("[TestController] /mapper-test 매퍼 동작 테스트");
        int count = userMapper.countUserById("test@test.com");
        log.info("[TestController] userMapper.countUserById('test@test.com') = {}", count);
        return "매퍼 정상 동작! count = " + count;
    }

    // 로그인된 사용자의 인증 정보 확인용 테스트 엔드포인트
    @GetMapping("/test/auth-info")
    public Map<String, Object> authInfo(Authentication authentication,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.debug("[TestController] /test/auth-info 요청 수신");
        Map<String, Object> result = new HashMap<>();
        if (authentication == null) {
            result.put("authenticated", false);
            result.put("message", "로그인되지 않은 상태입니다.");
            return result;
        }

        result.put("authenticated", authentication.isAuthenticated());
        result.put("authType", authentication.getClass().getSimpleName());
        result.put("username", authentication.getName());
        result.put("authorities", authentication.getAuthorities().toString());

        if (userDetails != null) {
            result.put("user_no", userDetails.getUser_no());
            result.put("userDetailsType", userDetails.getClass().getSimpleName());
        }
        return result;
    }

    // 로그인된 사용자의 Todo 통계 조회 테스트
    @GetMapping("/test/stats")
    public TodoStatsDTO testStats(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            log.warn("[TestController] /test/stats - 인증 정보 없음");
            return new TodoStatsDTO();
        }
        Long userNo = userDetails.getUser_no();
        log.debug("[TestController] /test/stats - user_no={}", userNo);
        return todoService.getTodoStats(userNo);
    }

    // 관리자(ROLE_ADMIN) 전용 테스트 엔드포인트
    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        log.info("[TestController] /admin/dashboard 관리자 접근 성공");
        return "관리자 전용 대시보드입니다. 환영합니다!";
    }
}