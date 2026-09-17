package kr.or.oti.project.service.impl;

import kr.or.oti.project.domain.Todo;
import kr.or.oti.project.dto.PageRequestDTO;
import kr.or.oti.project.dto.TodoResponseDto;
import kr.or.oti.project.dto.TodoSaveRequestDto;
import kr.or.oti.project.dto.TodoUpdateRequestDto;
import kr.or.oti.project.mapper.TodoMapper;
import kr.or.oti.project.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
        log.info("일정 등록 완료 - user_id={}, title={}", user_id, dto.getTitle()); // 추가
    }

    // 목록 조회
    
    @Override
    public List<TodoResponseDto> getTodoList(PageRequestDTO pag) {
        log.debug("목록 조회 - user_id={}, keyword={}, page={}", pag.getUser_id(), pag.getKeyword(), pag.getPage());
        return todoMapper.selectTodoList(pag).stream()
                .map(TodoResponseDto::from)
                .collect(Collectors.toList());
    }

    // 상세 조회 - 존재 여부 확인 후, 본인의 일정인지 소유권 검증
    @Override
    public TodoResponseDto getTodo(Long todo_id, String user_id) {
        Todo todo = todoMapper.selectTodoById(todo_id);

        if (todo == null) {
            throw new IllegalArgumentException("존재하지 않는 일정입니다.");
        }

        // 남의 일정 조회 시도 차단 (소유권 검증)
        if (!todo.getUser_id().equals(user_id)) {
            log.warn("소유권 불일치 - todo_id={}, 작성자={}, 요청자={}",
                    todo_id, todo.getUser_id(), user_id); // 추가 (existing 대신 todo 사용, 이 메서드는 변수명이 todo)
            throw new AccessDeniedException("해당 일정을 조회할 권한이 없습니다.");
        }

        return TodoResponseDto.from(todo);
    }

    // 수정 - 존재 여부 확인 후, 본인의 일정인지 소유권 검증 후 수정 실행
    @Override
    public void updateTodo(TodoUpdateRequestDto dto, String user_id) {
        Todo existing = todoMapper.selectTodoById(dto.getTodo_id());

        if (existing == null) {
            throw new IllegalArgumentException("수정할 일정을 찾을 수 없습니다.");
        }

        // 남의 일정 수정 시도 차단 (소유권 검증)
        if (!existing.getUser_id().equals(user_id)) {
            log.warn("소유권 불일치 - todo_id={}, 작성자={}, 요청자={}",
                    dto.getTodo_id(), existing.getUser_id(), user_id);
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
            throw new IllegalArgumentException("수정할 일정을 찾을 수 없습니다.");
        }
        log.info("일정 수정 완료 - user_id={}, todo_id={}", user_id, dto.getTodo_id());
    }

    // 삭제 - 존재 여부 확인 후, 본인의 일정인지 소유권 검증 후 삭제 실행
    @Override
    public void deleteTodo(Long todo_id, String user_id) {
        Todo existing = todoMapper.selectTodoById(todo_id);

        if (existing == null) {
            throw new IllegalArgumentException("삭제할 일정을 찾을 수 없습니다.");
        }

        // 남의 일정 삭제 시도 차단 (소유권 검증)
        if (!existing.getUser_id().equals(user_id)) {
            log.warn("소유권 불일치 - todo_id={}, 작성자={}, 요청자={}",
                    todo_id, existing.getUser_id(), user_id);
            throw new AccessDeniedException("해당 일정을 삭제할 권한이 없습니다.");
        }

        int deletedRows = todoMapper.deleteTodo(todo_id);
        if (deletedRows == 0) {
            throw new IllegalArgumentException("삭제할 일정을 찾을 수 없습니다.");
        }
        log.info("일정 삭제 완료 - user_id={}, todo_id={}", user_id, todo_id);
    }

    // 제목 검색
    
    @Override
    public int getTotalCount(PageRequestDTO pag) {
        return todoMapper.getTotalCount(pag);
    }
}