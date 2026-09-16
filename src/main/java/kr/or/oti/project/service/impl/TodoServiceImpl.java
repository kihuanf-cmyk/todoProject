package kr.or.oti.project.service.impl;

import kr.or.oti.project.domain.Todo;
import kr.or.oti.project.dto.TodoResponseDto;
import kr.or.oti.project.dto.TodoSaveRequestDto;
import kr.or.oti.project.dto.TodoUpdateRequestDto;
import kr.or.oti.project.mapper.TodoMapper;
import kr.or.oti.project.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoMapper todoMapper;

    // 등록
    @Override
    public void saveTodo(TodoSaveRequestDto dto, String user_id) {
        Todo todo = new Todo();
        todo.setUser_id(user_id);
        todo.setTitle(dto.getTitle());
        todo.setContent(dto.getContent());
        todo.setSchedule_date(dto.getSchedule_date());
        todo.setIs_checked("N");
        todoMapper.insertTodo(todo);
    }

    // 목록 조회
    @Override
    public List<TodoResponseDto> getTodoList(String user_id) {
        return todoMapper.selectTodoListByUser(user_id).stream()
                .map(TodoResponseDto::from)
                .collect(Collectors.toList());
    }

    // 상세 조회 - 존재 여부 확인 후, 본인의 일정인지 소유권 검증
    @Override
    public TodoResponseDto getTodo(Long todo_id, String user_id) {
        Todo todo = todoMapper.selectTodoById(todo_id);

        if (todo == null) {
            throw new IllegalArgumentException("존재하지 않는 일정입니다. (id: " + todo_id + ")");
        }

        // 남의 일정 조회 시도 차단 (소유권 검증)
        if (!todo.getUser_id().equals(user_id)) {
            throw new AccessDeniedException("해당 일정을 조회할 권한이 없습니다.");
        }

        return TodoResponseDto.from(todo);
    }

    // 수정 - 존재 여부 확인 후, 본인의 일정인지 소유권 검증 후 수정 실행
    @Override
    public void updateTodo(TodoUpdateRequestDto dto, String user_id) {
        Todo existing = todoMapper.selectTodoById(dto.getTodo_id());

        if (existing == null) {
            throw new IllegalArgumentException("수정할 일정을 찾을 수 없습니다. (id: " + dto.getTodo_id() + ")");
        }

        // 남의 일정 수정 시도 차단 (소유권 검증)
        if (!existing.getUser_id().equals(user_id)) {
            throw new AccessDeniedException("해당 일정을 수정할 권한이 없습니다.");
        }

        Todo todo = new Todo();
        todo.setTodo_id(dto.getTodo_id());
        todo.setTitle(dto.getTitle());
        todo.setContent(dto.getContent());
        todo.setSchedule_date(dto.getSchedule_date());
        todo.setIs_checked(dto.getIs_checked());

        int updatedRows = todoMapper.updateTodo(todo);
        if (updatedRows == 0) {
            throw new IllegalArgumentException("수정할 일정을 찾을 수 없습니다. (id: " + dto.getTodo_id() + ")");
        }
    }

    // 삭제 - 존재 여부 확인 후, 본인의 일정인지 소유권 검증 후 삭제 실행
    @Override
    public void deleteTodo(Long todo_id, String user_id) {
        Todo existing = todoMapper.selectTodoById(todo_id);

        if (existing == null) {
            throw new IllegalArgumentException("삭제할 일정을 찾을 수 없습니다. (id: " + todo_id + ")");
        }

        // 남의 일정 삭제 시도 차단 (소유권 검증)
        if (!existing.getUser_id().equals(user_id)) {
            throw new AccessDeniedException("해당 일정을 삭제할 권한이 없습니다.");
        }

        int deletedRows = todoMapper.deleteTodo(todo_id);
        if (deletedRows == 0) {
            throw new IllegalArgumentException("삭제할 일정을 찾을 수 없습니다. (id: " + todo_id + ")");
        }
    }

    // 제목 검색
    @Override
    public List<TodoResponseDto> searchTodoByTitle(String user_id, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getTodoList(user_id);
        }
        return todoMapper.searchTodoByTitle(user_id, keyword).stream()
                .map(TodoResponseDto::from)
                .collect(Collectors.toList());
    }
}