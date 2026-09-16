package kr.or.oti.project.service;

import java.util.List;

import kr.or.oti.project.dto.TodoSaveRequestDto;
import kr.or.oti.project.dto.TodoResponseDto;
import kr.or.oti.project.dto.TodoUpdateRequestDto;


public interface TodoService {
	 void saveTodo(TodoSaveRequestDto dto, String user_id);
	    List<TodoResponseDto> getTodoList(String user_id);
	    TodoResponseDto getTodo(Long todo_id);
	    void updateTodo(TodoUpdateRequestDto dto);
	    void deleteTodo(Long todo_id);
}
