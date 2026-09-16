package kr.or.oti.project.service;

import java.util.List;

import kr.or.oti.project.dto.TodoSaveRequestDto;
import kr.or.oti.project.dto.TodoResponseDto;
import kr.or.oti.project.dto.TodoUpdateRequestDto;

public interface TodoService {
    void saveTodo(TodoSaveRequestDto dto, String user_id);
    List<TodoResponseDto> getTodoList(String user_id);

    // 상세 조회 시 본인의 일정인지 검증하기 위해 user_id 추가
    TodoResponseDto getTodo(Long todo_id, String user_id);

    // 수정 시 본인의 일정인지 검증하기 위해 user_id 추가
    void updateTodo(TodoUpdateRequestDto dto, String user_id);

    // 삭제 시 본인의 일정인지 검증하기 위해 user_id 추가
    void deleteTodo(Long todo_id, String user_id);

    // 검색 기능
    List<TodoResponseDto> searchTodoByTitle(String user_id, String keyword);
}
