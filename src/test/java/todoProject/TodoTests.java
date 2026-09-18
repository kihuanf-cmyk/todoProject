package todoProject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import kr.or.oti.project.domain.Todo;
import kr.or.oti.project.dto.TodoResponseDto;
import kr.or.oti.project.dto.TodoSaveRequestDto;
import kr.or.oti.project.mapper.TodoMapper;
import kr.or.oti.project.service.impl.TodoServiceImpl;

// 테스트 작성일: 2026-09-17
// 테스트 순서: 도메인 -> 서비스 -> DB/매퍼 -> 컨트롤러 -> DTO
@ExtendWith(MockitoExtension.class)
class TodoTests {

	@Nested
	@DisplayName("Todo 도메인 단위 테스트")
	class Todo도메인테스트 {

		@Test
		@DisplayName("새 Todo 객체는 필드에 입력한 값을 그대로 보관한다")
		void 새Todo객체는입력한값을그대로보관한다() {
			Todo todo = new Todo();
			Date scheduleDate = Date.valueOf("2026-09-17");

			todo.setTodo_id(1L);
			todo.setUser_id("user01");
			todo.setTitle("병원 예약");
			todo.setContent("오후 2시에 방문");
			todo.setSchedule_date(scheduleDate);
			todo.setIs_checked("N");

			assertThat(todo.getTodo_id()).isEqualTo(1L);
			assertThat(todo.getUser_id()).isEqualTo("user01");
			assertThat(todo.getTitle()).isEqualTo("병원 예약");
			assertThat(todo.getContent()).isEqualTo("오후 2시에 방문");
			assertThat(todo.getSchedule_date()).isEqualTo(scheduleDate);
			assertThat(todo.getIs_checked()).isEqualTo("N");
		}

		@Test
		@DisplayName("TodoResponseDto는 Todo의 화면 표시용 값을 옮겨 담는다")
		void TodoResponseDto는Todo값을옮겨담는다() {
			Todo todo = new Todo();
			todo.setTodo_id(2L);
			todo.setTitle("운동");
			todo.setContent("30분 걷기");
			todo.setSchedule_date(Date.valueOf("2026-09-18"));
			todo.setIs_checked("Y");

			TodoResponseDto response = TodoResponseDto.from(todo);

			assertThat(response.getTodo_id()).isEqualTo(2L);
			assertThat(response.getTitle()).isEqualTo("운동");
			assertThat(response.getContent()).isEqualTo("30분 걷기");
			assertThat(response.getSchedule_date()).isEqualTo(todo.getSchedule_date());
			assertThat(response.getIs_checked()).isEqualTo("Y");
		}
	}

	@Nested
	@DisplayName("Todo 서비스 통합 테스트")
	@ExtendWith(MockitoExtension.class)
	class Todo서비스테스트 {

		@Mock
		private TodoMapper todoMapper;

		@InjectMocks
		private TodoServiceImpl todoService;

		@Test
		@DisplayName("Todo를 등록하면 로그인한 사용자의 일정으로 저장하고 미완료 상태를 기본 설정한다")
		void Todo를등록하면사용자와미완료상태를저장한다() {
			TodoSaveRequestDto request = new TodoSaveRequestDto();
			request.setTitle("공부");
			request.setContent("테스트 코드 읽기");
			request.setSchedule_date(Date.valueOf("2026-09-17"));

			todoService.saveTodo(request, "user01");

			ArgumentCaptor<Todo> savedTodo = ArgumentCaptor.forClass(Todo.class);
			verify(todoMapper).insertTodo(savedTodo.capture());
			assertThat(savedTodo.getValue().getUser_id()).isEqualTo("user01");
			assertThat(savedTodo.getValue().getTitle()).isEqualTo("공부");
			assertThat(savedTodo.getValue().getContent()).isEqualTo("테스트 코드 읽기");
			assertThat(savedTodo.getValue().getSchedule_date()).isEqualTo(request.getSchedule_date());
			assertThat(savedTodo.getValue().getIs_checked()).isEqualTo("N");
		}

		@Test
		@DisplayName("존재하지 않는 Todo를 조회하면 안내 메시지와 함께 예외가 발생한다")
		void 존재하지않는Todo를조회하면예외가발생한다() {
			when(todoMapper.selectTodoById(99L)).thenReturn(null);

			assertThatThrownBy(() -> todoService.getTodo(99L, "user01"))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("존재하지 않는 일정입니다.");
		}

		@Test
		@DisplayName("다른 사용자의 Todo를 조회하면 접근을 거부하고 추가 조회를 하지 않는다")
		void 다른사용자의Todo를조회하면접근을거부한다() {
			Todo todo = new Todo();
			todo.setTodo_id(1L);
			todo.setUser_id("owner");
			when(todoMapper.selectTodoById(1L)).thenReturn(todo);

			assertThatThrownBy(() -> todoService.getTodo(1L, "visitor"))
					.isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
					.hasMessage("해당 일정을 조회할 권한이 없습니다.");
			verify(todoMapper, never()).updateTodo(any(Todo.class));
		}

		@Test
		@DisplayName("Todo 목록이 비어 있으면 빈 화면 목록을 반환한다")
		void Todo목록이비어있으면빈목록을반환한다() {
			kr.or.oti.project.dto.PageRequestDTO pageRequest = new kr.or.oti.project.dto.PageRequestDTO();
			when(todoMapper.selectTodoList(pageRequest)).thenReturn(Collections.emptyList());

			assertThat(todoService.getTodoList(pageRequest)).isEmpty();
		}
	}
}
