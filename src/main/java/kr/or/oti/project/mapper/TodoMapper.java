package kr.or.oti.project.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.oti.project.domain.Todo;
import kr.or.oti.project.dto.PageRequestDTO;

@Mapper
public interface TodoMapper {
    int insertTodo(Todo todo);
    List<Todo> selectTodoList(PageRequestDTO pag);
    Todo selectTodoById(Long todo_id);
    int updateTodo(Todo todo);
    int updateTodoFile(Todo todo);
    int deleteTodoFile(Long todo_id);
    int deleteTodo(Long todo_id);

    // @Param 명시 : XML에서 #{user_no}, #{keyword} 로 정확히 매핑되도록 파라미터 이름을 고정
    int getTotalCount(PageRequestDTO pag);
    
    List<Map<String, Object>> selectStatusCountByUser(@Param("user_no") Long user_no);

    // 기존 인터페이스에 메서드 추가
    List<Todo> selectAllTodoByUser(@Param("user_no") Long user_no);
    
    // Kanban Drag&Drop 상태 변경용 - status만 업데이트, 본인 소유 검증을 위해 user_no도 WHERE 조건에 포함
    int updateStatus(@Param("todo_id") Long todo_id,
                      @Param("status") String status,
                      @Param("user_no") Long user_no);
}
