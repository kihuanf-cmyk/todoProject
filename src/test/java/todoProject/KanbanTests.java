package todoProject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import kr.or.oti.project.controller.KanbanController;
import kr.or.oti.project.domain.User;
import kr.or.oti.project.dto.TodoResponseDto;
import kr.or.oti.project.security.CustomUserDetails;
import kr.or.oti.project.service.TodoService;

@ExtendWith(MockitoExtension.class)
class KanbanTests {

    @Mock
    private TodoService todoService;

    @Mock
    private Model model;

    @InjectMocks
    private KanbanController kanbanController;

    @Test
    @DisplayName("칸반 보드는 전체 일정을 TODO, DOING, DONE 상태별로 분류하여 Model에 담는다")
    @SuppressWarnings("unchecked")
    void 칸반_보드_상태별_분류_테스트() {
        // Given
        User user = new User();
        user.setUser_no(1L);
        user.setUser_id("user01");
        CustomUserDetails userDetails = new CustomUserDetails(user);

        TodoResponseDto todo1 = TodoResponseDto.builder().todo_id(1L).title("작업1").status("TODO").schedule_date(Date.valueOf("2026-09-23")).build();
        TodoResponseDto todo2 = TodoResponseDto.builder().todo_id(2L).title("작업2").status("DOING").schedule_date(Date.valueOf("2026-09-23")).build();
        TodoResponseDto todo3 = TodoResponseDto.builder().todo_id(3L).title("작업3").status("DONE").schedule_date(Date.valueOf("2026-09-23")).build();
        TodoResponseDto todo4 = TodoResponseDto.builder().todo_id(4L).title("작업4").status("TODO").schedule_date(Date.valueOf("2026-09-24")).build();

        when(todoService.getAllTodoByUser(1L)).thenReturn(List.of(todo1, todo2, todo3, todo4));

        // When
        String viewName = kanbanController.kanban(userDetails, model);

        // Then
        assertThat(viewName).isEqualTo("kanban");

        ArgumentCaptor<List<TodoResponseDto>> todoListCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<TodoResponseDto>> doingListCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<TodoResponseDto>> doneListCaptor = ArgumentCaptor.forClass(List.class);

        verify(model).addAttribute(org.mockito.ArgumentMatchers.eq("todoList"), todoListCaptor.capture());
        verify(model).addAttribute(org.mockito.ArgumentMatchers.eq("doingList"), doingListCaptor.capture());
        verify(model).addAttribute(org.mockito.ArgumentMatchers.eq("doneList"), doneListCaptor.capture());

        assertThat(todoListCaptor.getValue()).hasSize(2);
        assertThat(todoListCaptor.getValue()).extracting(TodoResponseDto::getTitle).containsExactly("작업1", "작업4");

        assertThat(doingListCaptor.getValue()).hasSize(1);
        assertThat(doingListCaptor.getValue()).extracting(TodoResponseDto::getTitle).containsExactly("작업2");

        assertThat(doneListCaptor.getValue()).hasSize(1);
        assertThat(doneListCaptor.getValue()).extracting(TodoResponseDto::getTitle).containsExactly("작업3");
    }
}

