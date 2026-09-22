package kr.or.oti.project.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import kr.or.oti.project.domain.Todo;
import kr.or.oti.project.dto.PageRequestDTO;
import kr.or.oti.project.dto.TodoResponseDto;
import kr.or.oti.project.dto.TodoSaveRequestDto;
import kr.or.oti.project.dto.TodoStatsDTO;
import kr.or.oti.project.dto.TodoUpdateRequestDto;
import kr.or.oti.project.mapper.TodoMapper;
import kr.or.oti.project.service.TodoService;
import kr.or.oti.project.util.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoMapper todoMapper;
    private final FileStorageUtil fileStorageUtil;

    /**
     * 1단계: TODO 기본 정보를 저장해 todo_id를 확보한다.
     * 2단계: 파일이 있을 때만 디스크 저장 후 TODO 파일 컬럼을 갱신한다.
     */
    @Override
    @Transactional
    public void saveTodo(TodoSaveRequestDto dto, Long user_no, MultipartFile file) {
        Todo todo = new Todo();
        todo.setUser_no(user_no);
        todo.setTitle(dto.getTitle());
        todo.setContent(dto.getContent());
        todo.setSchedule_date(dto.getSchedule_date());
        todo.setStatus("TODO");

        todoMapper.insertTodo(todo);

        if (hasFile(file)) {
            saveAndUpdateFile(todo, file, user_no);
        }
        log.info("일정 등록 완료 - user_no={}, todo_id={}, hasFile={}",
                user_no, todo.getTodo_id(), hasFile(file));
    }

    @Override
    public List<TodoResponseDto> getTodoList(PageRequestDTO pag) {
        log.debug("일정 목록 조회 - user_no={}, keyword={}, page={}",
                pag.getUser_no(), pag.getKeyword(), pag.getPage());
        return todoMapper.selectTodoList(pag).stream()
                .map(TodoResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public TodoResponseDto getTodo(Long todo_id, Long user_no) {
        return TodoResponseDto.from(getOwnedTodo(todo_id, user_no));
    }

    /**
     * 새 파일이 있으면 교체가 우선한다. 없으면 deleteFile 값에 따라 삭제하거나 유지한다.
     * 새 파일 DB 반영 실패 시 새로 저장한 물리 파일을 즉시 정리해 고아 파일을 방지한다.
     */
    @Override
    @Transactional
    public void updateTodo(TodoUpdateRequestDto dto, Long user_no, MultipartFile file) {
        Todo existing = getOwnedTodo(dto.getTodo_id(), user_no);

        Todo todo = new Todo();
        todo.setTodo_id(dto.getTodo_id());
        todo.setUser_no(user_no);
        todo.setTitle(dto.getTitle());
        todo.setContent(dto.getContent());
        todo.setSchedule_date(dto.getSchedule_date());
        todo.setStatus(dto.getStatus());

        if (todoMapper.updateTodo(todo) == 0) {
            log.warn("일정 수정 대상 없음 - user_no={}, todo_id={}", user_no, dto.getTodo_id());
            throw new IllegalArgumentException("수정할 일정을 찾을 수 없습니다.");
        }

        if (hasFile(file)) {
            // 안전한 순서: 새 파일 저장/DB 반영 성공 후 기존 물리 파일을 삭제한다.
            saveAndUpdateFile(todo, file, user_no);
            fileStorageUtil.deleteFile(existing.getFile_url());
            log.info("첨부파일 교체 완료 - user_no={}, todo_id={}, originalName={}",
                    user_no, dto.getTodo_id(), file.getOriginalFilename());
        } else if (dto.isDeleteFile() && hasStoredFile(existing)) {
            if (todoMapper.deleteTodoFile(dto.getTodo_id()) == 0) {
                log.error("첨부파일 정보 삭제 실패 - user_no={}, todo_id={}", user_no, dto.getTodo_id());
                throw new IllegalStateException("첨부파일 정보를 삭제할 수 없습니다.");
            }
            fileStorageUtil.deleteFile(existing.getFile_url());
            log.info("첨부파일 삭제 완료 - user_no={}, todo_id={}", user_no, dto.getTodo_id());
        } else {
            log.debug("첨부파일 변경 없음 - user_no={}, todo_id={}, deleteFile={}",
                    user_no, dto.getTodo_id(), dto.isDeleteFile());
        }

        log.info("일정 수정 완료 - user_no={}, todo_id={}", user_no, dto.getTodo_id());
    }

    @Override
    public void deleteTodo(Long todo_id, Long user_no) {
        Todo existing = getOwnedTodo(todo_id, user_no);
        if (todoMapper.deleteTodo(todo_id) == 0) {
            log.warn("일정 삭제 대상 없음 - user_no={}, todo_id={}", user_no, todo_id);
            throw new IllegalArgumentException("삭제할 일정을 찾을 수 없습니다.");
        }
        fileStorageUtil.deleteFile(existing.getFile_url());
        log.info("일정 삭제 완료 - user_no={}, todo_id={}", user_no, todo_id);
    }

    @Override
    public int getTotalCount(PageRequestDTO pag) {
        return todoMapper.getTotalCount(pag);
    }

    private Todo getOwnedTodo(Long todo_id, Long user_no) {
        Todo todo = todoMapper.selectTodoById(todo_id);
        if (todo == null) {
            log.warn("존재하지 않는 일정 요청 - user_no={}, todo_id={}", user_no, todo_id);
            throw new IllegalArgumentException("존재하지 않는 일정입니다.");
        }
        if (!todo.getUser_no().equals(user_no)) {
            log.warn("일정 소유권 불일치 - todo_id={}, owner={}, requester={}",
                    todo_id, todo.getUser_no(), user_no);
            throw new AccessDeniedException("해당 일정에 접근할 권한이 없습니다.");
        }
        return todo;
    }
    
    @Override
    public TodoStatsDTO getTodoStats(Long user_no) {

        // 1. Mapper 호출 - status별 개수를 담은 List<Map> 조회
        List<Map<String, Object>> rows = todoMapper.selectStatusCountByUser(user_no);
        log.debug("통계 집계 조회 - user_no={}, rows={}", user_no, rows);

        // 2. 값을 채울 그릇(DTO) 준비
        TodoStatsDTO stats = new TodoStatsDTO();
        long todo_count = 0;
        long doing_count = 0;
        long done_count = 0;

        // 3. 행을 순회하며 status에 맞는 변수에 개수 누적
        for (Map<String, Object> row : rows) {
            String status = (String) row.get("STATUS");
            long cnt = ((Number) row.get("CNT")).longValue();

            switch (status) {
                case "TODO":
                    todo_count = cnt;
                    break;
                case "DOING":
                    doing_count = cnt;
                    break;
                case "DONE":
                    done_count = cnt;
                    break;
            }
        }

        // 4. 전체 개수 = 세 상태 합
        long total_count = todo_count + doing_count + done_count;

        // 5. 완료율 계산 (0으로 나누기 방지)
        double completion_rate = (total_count == 0)
                ? 0.0
                : (done_count * 100.0 / total_count);

        // 6. DTO에 setter로 값 채우기
        stats.setTotal_count(total_count);
        stats.setTodo_count(todo_count);
        stats.setDoing_count(doing_count);
        stats.setDone_count(done_count);
        stats.setCompletion_rate(completion_rate);
        
        log.debug("통계 계산 완료 - user_no={}, total={}, todo={}, doing={}, done={}, completionRate={}", // 추가: 계산 결과 로그
                user_no, total_count, todo_count, doing_count, done_count, completion_rate);


        return stats;
    }
    private void saveAndUpdateFile(Todo todo, MultipartFile file, Long user_no) {
        String savedName = null;
        try {
            savedName = fileStorageUtil.storeFile(file);
            todo.setFile_url(savedName);
            todo.setFile_name(file.getOriginalFilename());

            if (todoMapper.updateTodoFile(todo) == 0) {
                log.error("첨부파일 정보 저장 실패 - user_no={}, todo_id={}", user_no, todo.getTodo_id());
                throw new IllegalStateException("첨부파일 정보를 저장할 수 없습니다.");
            }
            log.debug("첨부파일 정보 저장 완료 - todo_id={}, originalName={}, savedName={}",
                    todo.getTodo_id(), file.getOriginalFilename(), savedName);
        } catch (RuntimeException e) {
            // DB 반영에 실패한 새 파일은 참조되지 않으므로 즉시 제거한다.
            fileStorageUtil.deleteFile(savedName);
            throw e;
        }
    }

    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private boolean hasStoredFile(Todo todo) {
        return todo.getFile_url() != null && !todo.getFile_url().isBlank();
    }
}
