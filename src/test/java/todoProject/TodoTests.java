package todoProject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import kr.or.oti.project.domain.Todo;
import kr.or.oti.project.dto.PageRequestDTO;
import kr.or.oti.project.dto.TodoResponseDto;
import kr.or.oti.project.dto.TodoSaveRequestDto;
import kr.or.oti.project.dto.TodoUpdateRequestDto;
import kr.or.oti.project.mapper.TodoMapper;
import kr.or.oti.project.service.impl.TodoServiceImpl;
import kr.or.oti.project.util.FileStorageUtil;

@ExtendWith(MockitoExtension.class)
class TodoTests {

    @Mock
    private TodoMapper todoMapper;

    @Mock
    private FileStorageUtil fileStorageUtil;

    @InjectMocks
    private TodoServiceImpl todoService;

    @Test
    @DisplayName("등록은 기본 정보 저장 후 첨부파일이 있으면 파일 컬럼을 갱신한다")
    void 등록은기본정보저장후첨부파일컬럼을갱신한다() {
        TodoSaveRequestDto request = new TodoSaveRequestDto();
        request.setTitle("회의");
        request.setContent("회의 내용");
        request.setSchedule_date(Date.valueOf("2026-09-21"));

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("plan.pdf");
        when(fileStorageUtil.storeFile(file)).thenReturn("uuid_plan.pdf");
        when(todoMapper.updateTodoFile(any(Todo.class))).thenReturn(1);

        todoService.saveTodo(request, 1L, file);

        verify(todoMapper).insertTodo(any(Todo.class));
        verify(todoMapper).updateTodoFile(any(Todo.class));
    }

    @Test
    @DisplayName("새 파일로 수정하면 기존 파일을 삭제하고 파일 정보를 교체한다")
    void 새파일로수정하면기존파일을삭제하고파일정보를교체한다() {
        Todo existing = ownedTodo("old.pdf");
        when(todoMapper.selectTodoById(1L)).thenReturn(existing);
        when(todoMapper.updateTodo(any(Todo.class))).thenReturn(1);
        when(todoMapper.updateTodoFile(any(Todo.class))).thenReturn(1);

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("new.pdf");
        when(fileStorageUtil.storeFile(file)).thenReturn("new.pdf");

        todoService.updateTodo(updateRequest(false), 1L, file);

        verify(todoMapper).updateTodoFile(any(Todo.class));
        verify(fileStorageUtil).deleteFile("old.pdf");
    }

    @Test
    @DisplayName("삭제 표시 후 저장하면 파일 컬럼과 물리 파일을 삭제한다")
    void 삭제표시후저장하면파일컬럼과물리파일을삭제한다() {
        when(todoMapper.selectTodoById(1L)).thenReturn(ownedTodo("file.pdf"));
        when(todoMapper.updateTodo(any(Todo.class))).thenReturn(1);
        when(todoMapper.deleteTodoFile(1L)).thenReturn(1);

        todoService.updateTodo(updateRequest(true), 1L, null);

        verify(todoMapper).deleteTodoFile(1L);
        verify(fileStorageUtil).deleteFile("file.pdf");
    }

    @Test
    @DisplayName("파일 변경이 없으면 파일 관련 Mapper를 호출하지 않는다")
    void 파일변경이없으면파일관련Mapper를호출하지않는다() {
        when(todoMapper.selectTodoById(1L)).thenReturn(ownedTodo("file.pdf"));
        when(todoMapper.updateTodo(any(Todo.class))).thenReturn(1);

        todoService.updateTodo(updateRequest(false), 1L, null);

        verify(todoMapper, never()).updateTodoFile(any(Todo.class));
        verify(todoMapper, never()).deleteTodoFile(any(Long.class));
    }

    @Test
    @DisplayName("다른 사용자의 Todo 상세 조회를 시도하면 AccessDeniedException을 던진다")
    void 다른사용자의Todo접근은거부한다() {
        Todo todo = new Todo();
        todo.setUser_no(1L);
        when(todoMapper.selectTodoById(1L)).thenReturn(todo);

        assertThatThrownBy(() -> todoService.getTodo(1L, 2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("해당 일정에 접근할 권한이 없습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 일정 번호 조회 시 IllegalArgumentException을 던진다")
    void 존재하지않는_일정조회시_IllegalArgumentException이_발생한다() {
        when(todoMapper.selectTodoById(999L)).thenReturn(null);

        assertThatThrownBy(() -> todoService.getTodo(999L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 일정입니다.");
    }

    @Test
    @DisplayName("본인의 일정인 경우 상세 조회가 정상 작동한다")
    void 본인일정_조회시_정상적으로_DTO를_반환한다() {
        Todo todo = ownedTodo("file.pdf");
        todo.setTitle("테스트 제목");
        when(todoMapper.selectTodoById(1L)).thenReturn(todo);

        TodoResponseDto result = todoService.getTodo(1L, 1L);

        assertThat(result.getTodo_id()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("테스트 제목");
    }

    @Test
    @DisplayName("본인의 일정을 삭제할 경우 DB 삭제 및 첨부파일을 원자적으로 삭제한다")
    void 본인일정_삭제시_DB와파일을_삭제한다() {
        Todo todo = ownedTodo("sample.pdf");
        when(todoMapper.selectTodoById(1L)).thenReturn(todo);
        when(todoMapper.deleteTodo(1L)).thenReturn(1);

        todoService.deleteTodo(1L, 1L);

        verify(todoMapper).deleteTodo(1L);
        verify(fileStorageUtil).deleteFile("sample.pdf");
    }

    @Test
    @DisplayName("다른 사용자의 일정을 삭제 시도할 경우 AccessDeniedException을 던진다")
    void 다른사용자_일정삭제시_AccessDeniedException이_발생한다() {
        Todo todo = ownedTodo("sample.pdf");
        when(todoMapper.selectTodoById(1L)).thenReturn(todo);

        assertThatThrownBy(() -> todoService.deleteTodo(1L, 2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("해당 일정에 접근할 권한이 없습니다.");
    }

    @Test
    @DisplayName("페이지 및 검색 조건으로 목록 조회 시 Mapper를 올바르게 호출한다")
    void 페이징조회시_Mapper를_호출하고_DTO목록을_반환한다() {
        PageRequestDTO pageReq = new PageRequestDTO();
        pageReq.setUser_no(1L);
        pageReq.setPage(1);
        pageReq.setAmount(10);
        pageReq.setKeyword("회의");

        Todo todo = ownedTodo(null);
        todo.setTitle("회의 준비");
        when(todoMapper.selectTodoList(pageReq)).thenReturn(List.of(todo));

        List<TodoResponseDto> result = todoService.getTodoList(pageReq);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("회의 준비");
        verify(todoMapper).selectTodoList(pageReq);
    }

    @Test
    @DisplayName("전체 일정 개수 조회 시 getTotalCount Mapper를 호출한다")
    void 전체개수조회시_getTotalCount_Mapper를_호출한다() {
        PageRequestDTO pageReq = new PageRequestDTO();
        pageReq.setUser_no(1L);

        when(todoMapper.getTotalCount(pageReq)).thenReturn(15);

        int count = todoService.getTotalCount(pageReq);

        assertThat(count).isEqualTo(15);
        verify(todoMapper).getTotalCount(pageReq);
    }

    private Todo ownedTodo(String fileUrl) {
        Todo todo = new Todo();
        todo.setTodo_id(1L);
        todo.setUser_no(1L);
        todo.setFile_url(fileUrl);
        return todo;
    }

    private TodoUpdateRequestDto updateRequest(boolean deleteFile) {
        TodoUpdateRequestDto request = new TodoUpdateRequestDto();
        request.setTodo_id(1L);
        request.setTitle("수정");
        request.setContent("수정 내용");
        request.setSchedule_date(Date.valueOf("2026-09-21"));
        request.setStatus("TODO");
        request.setDeleteFile(deleteFile);
        return request;
    }
}
