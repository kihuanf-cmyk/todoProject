package todoProject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.or.oti.project.controller.TodoController;
import kr.or.oti.project.domain.User;
import kr.or.oti.project.dto.PageRequestDTO;
import kr.or.oti.project.dto.TodoResponseDto;
import kr.or.oti.project.dto.TodoSaveRequestDto;
import kr.or.oti.project.dto.TodoUpdateRequestDto;
import kr.or.oti.project.security.CustomUserDetails;
import kr.or.oti.project.service.TodoService;

@ExtendWith(MockitoExtension.class)
class TodoControllerTests {

    @Mock
    private TodoService todoService;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private TodoController todoController;

    private CustomUserDetails createMockUserDetails(Long userNo, String userId) {
        User user = new User();
        user.setUser_no(userNo);
        user.setUser_id(userId);
        return new CustomUserDetails(user);
    }

    @Test
    @DisplayName("목록 조회는 페이징과 검색조건을 설정하고 list 뷰를 반환한다")
    void 목록_조회_성공() {
        CustomUserDetails userDetails = createMockUserDetails(1L, "user01");
        when(todoService.getTodoList(any(PageRequestDTO.class))).thenReturn(List.of());
        when(todoService.getTotalCount(any(PageRequestDTO.class))).thenReturn(0);

        String view = todoController.list(model, userDetails, "회의", 1);

        assertThat(view).isEqualTo("todo/list");
        verify(model).addAttribute(eq("todoList"), any());
        verify(model).addAttribute(eq("keyword"), eq("회의"));
        verify(model).addAttribute(eq("pageResponseDTO"), any());
    }

    @Test
    @DisplayName("상세 조회는 prev_url을 모델에 담고 read 뷰를 반환한다")
    void 상세_조회_성공() {
        CustomUserDetails userDetails = createMockUserDetails(1L, "user01");
        TodoResponseDto dto = TodoResponseDto.builder().todo_id(10L).title("일정").build();
        when(todoService.getTodo(10L, 1L)).thenReturn(dto);

        String view = todoController.detail(10L, 2, "검색어", "/kanban", userDetails, model);

        assertThat(view).isEqualTo("todo/read");
        verify(model).addAttribute("todo", dto);
        verify(model).addAttribute("page", 2);
        verify(model).addAttribute("keyword", "검색어");
        verify(model).addAttribute("prev_url", "/kanban");
    }

    @Test
    @DisplayName("수정 화면 요청 시 수정 폼에 필요한 데이터와 prev_url을 모델에 담는다")
    void 수정화면_요청_성공() {
        CustomUserDetails userDetails = createMockUserDetails(1L, "user01");
        TodoResponseDto dto = TodoResponseDto.builder().todo_id(10L).title("일정").build();
        when(todoService.getTodo(10L, 1L)).thenReturn(dto);

        String view = todoController.modifyForm(10L, 1, null, "/calendar", userDetails, model);

        assertThat(view).isEqualTo("todo/modify");
        verify(model).addAttribute("todo", dto);
        verify(model).addAttribute("prev_url", "/calendar");
    }

    @Test
    @DisplayName("일정 수정 완료 시 prev_url이 전달되면 해당 URL로 리다이렉트한다")
    void 일정_수정_후_prev_url_리다이렉트() {
        CustomUserDetails userDetails = createMockUserDetails(1L, "user01");
        TodoUpdateRequestDto request = new TodoUpdateRequestDto();
        request.setTodo_id(10L);
        request.setTitle("수정 제목");
        request.setContent("수정 내용");
        request.setSchedule_date(Date.valueOf("2026-09-23"));
        request.setStatus("DOING");
        request.setPrev_url("/kanban");

        String redirect = todoController.update(request, userDetails, redirectAttributes, null);

        assertThat(redirect).isEqualTo("redirect:/kanban");
        verify(todoService).updateTodo(eq(request), eq(1L), any());
    }

    @Test
    @DisplayName("일정 삭제 완료 시 prev_url로 리다이렉트한다")
    void 일정_삭제_후_prev_url_리다이렉트() {
        CustomUserDetails userDetails = createMockUserDetails(1L, "user01");

        String redirect = todoController.delete(10L, "/", userDetails);

        assertThat(redirect).isEqualTo("redirect:/");
        verify(todoService).deleteTodo(10L, 1L);
    }

    @Test
    @DisplayName("AJAX 상태 변경 엔드포인트는 updateStatus를 호출하고 200 OK를 응답한다")
    void AJAX_상태_변경_엔드포인트_성공() {
        CustomUserDetails userDetails = createMockUserDetails(1L, "user01");

        ResponseEntity<Void> response = todoController.updateStatus(10L, "DONE", userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(todoService).updateStatus(10L, "DONE", 1L);
    }
}

