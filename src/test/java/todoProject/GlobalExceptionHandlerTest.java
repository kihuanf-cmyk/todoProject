package todoProject;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import kr.or.oti.project.controller.GlobalExceptionHandler;

@DisplayName("GlobalExceptionHandler 예외 처리기 테스트")
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestExceptionController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("IllegalArgumentException 발생 시 404 에러 화면과 메시지를 반환한다")
    void illegalArgumentException_처리_테스트() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/error"))
                .andExpect(model().attribute("errorCode", "404"))
                .andExpect(model().attribute("errorTitle", "항목을 찾을 수 없습니다"))
                .andExpect(model().attribute("errorMessage", "존재하지 않는 일정입니다."));
    }

    @Test
    @DisplayName("AccessDeniedException 발생 시 403 에러 화면과 메시지를 반환한다")
    void accessDeniedException_처리_테스트() throws Exception {
        mockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/error"))
                .andExpect(model().attribute("errorCode", "403"))
                .andExpect(model().attribute("errorTitle", "접근 권한이 없습니다"))
                .andExpect(model().attribute("errorMessage", "해당 데이터에 접근할 권한이 없습니다."));
    }

    @Test
    @DisplayName("MaxUploadSizeExceededException 발생 시 400 에러 화면과 메시지를 반환한다")
    void maxUploadSizeExceededException_처리_테스트() throws Exception {
        mockMvc.perform(post("/test/file-too-large"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/error"))
                .andExpect(model().attribute("errorCode", "400"))
                .andExpect(model().attribute("errorTitle", "첨부파일 용량이 너무 큽니다"));
    }

    @Test
    @DisplayName("MethodArgumentTypeMismatchException 발생 시 400 에러 화면과 메시지를 반환한다")
    void typeMismatchException_처리_테스트() throws Exception {
        mockMvc.perform(get("/test/mismatch/abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/error"))
                .andExpect(model().attribute("errorCode", "400"))
                .andExpect(model().attribute("errorTitle", "잘못된 요청 형식입니다"));
    }

    /**
     * 예외 발생 테스트를 위한 가짜 컨트롤러
     */
    @RestController
    static class TestExceptionController {

        @GetMapping("/test/not-found")
        public void throwNotFound() {
            throw new IllegalArgumentException("존재하지 않는 일정입니다.");
        }

        @GetMapping("/test/access-denied")
        public void throwAccessDenied() {
            throw new AccessDeniedException("해당 데이터에 접근할 권한이 없습니다.");
        }

        @PostMapping("/test/file-too-large")
        public void throwFileTooLarge() {
            throw new MaxUploadSizeExceededException(10 * 1024 * 1024);
        }

        @GetMapping("/test/mismatch/{id}")
        public void throwTypeMismatch(@PathVariable Long id) {
            // 숫자가 아닌 문자가 파라미터로 전송되면 스프링이 TypeMismatchException 발생시킴
        }
    }
}
