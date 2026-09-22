package kr.or.oti.project.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import kr.or.oti.project.dto.PageRequestDTO;
import kr.or.oti.project.dto.TodoResponseDto;
import kr.or.oti.project.dto.TodoSaveRequestDto;
import kr.or.oti.project.dto.TodoUpdateRequestDto;

public interface TodoService {
    void saveTodo(TodoSaveRequestDto dto, Long user_no, MultipartFile file);
    List<TodoResponseDto> getTodoList(PageRequestDTO pag);

    // 상세 조회 시 본인의 일정인지 검증하기 위해 user_no 사용
    TodoResponseDto getTodo(Long todo_id, Long user_no);

    // 수정 시 본인의 일정인지 검증하기 위해 user_no 사용
    void updateTodo(TodoUpdateRequestDto dto, Long user_no, MultipartFile file);

    // 삭제 시 본인의 일정인지 검증하기 위해 user_no 사용
    void deleteTodo(Long todo_id, Long user_no);

    // 검색 및 페이징용 총 개수
    int getTotalCount(PageRequestDTO pag);
}
