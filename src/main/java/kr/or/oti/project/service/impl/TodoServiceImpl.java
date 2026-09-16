package kr.or.oti.project.service.impl;

import kr.or.oti.project.domain.Todo;
import kr.or.oti.project.dto.TodoResponseDto;
import kr.or.oti.project.dto.TodoSaveRequestDto;
import kr.or.oti.project.dto.TodoUpdateRequestDto;
import kr.or.oti.project.mapper.TodoMapper;
import kr.or.oti.project.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoMapper todoMapper;

 // 등록 - user_id는 세션에서 꺼내 Controller가 넘겨줌 (String 타입으로 변경됨)
    @Override
    public void saveTodo(TodoSaveRequestDto dto, String user_id) {
        Todo todo = new Todo();
        todo.setUser_id(user_id);
        todo.setTitle(dto.getTitle());
        todo.setContent(dto.getContent());
        todo.setSchedule_date(dto.getSchedule_date()); // DTO는 Date 타입이라고 가정
        todo.setIs_checked("N"); // 새로 등록되는 일정은 항상 미완료 상태로 시작
        // insertTodo가 int(영향받은 행 수)를 리턴하지만 여기선 결과 안 씀
        todoMapper.insertTodo(todo);
    }

    // 목록 조회
    @Override
    public List<TodoResponseDto> getTodoList(String user_id) {
        return todoMapper.selectTodoListByUser(user_id).stream()
                .map(TodoResponseDto::from)
                .collect(Collectors.toList());
    }

    // 상세 조회
    @Override
    public TodoResponseDto getTodo(Long todo_id) {
        Todo todo = todoMapper.selectTodoById(todo_id);
        return TodoResponseDto.from(todo);
    }

    // 수정
    @Override
    public void updateTodo(TodoUpdateRequestDto dto) {
        Todo todo = new Todo();
        todo.setTodo_id(dto.getTodo_id());
        todo.setTitle(dto.getTitle());
        todo.setContent(dto.getContent());
        todo.setSchedule_date(dto.getSchedule_date());
        todo.setIs_checked(dto.getIs_checked());
        todoMapper.updateTodo(todo);
    }

    // 삭제
    @Override
    public void deleteTodo(Long todo_id) {
        todoMapper.deleteTodo(todo_id);
    }

//    // 제목 검색 - 새로 추가된 기능
//    @Override
//    public List<TodoResponseDto> searchTodoByTitle(String user_id, String keyword) {
//        return todoMapper.searchTodoByTitle(user_id, keyword).stream()
//                .map(TodoServiceImpl::toResponseDto)
//                .collect(Collectors.toList());
//    }

}