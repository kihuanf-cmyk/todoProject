package kr.or.oti.project.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import kr.or.oti.project.domain.Todo;
import kr.or.oti.project.dto.PageRequestDTO;

@Mapper
public interface TodoMapper {
    int insertTodo(Todo todo);
    List<Todo> selectTodoList(PageRequestDTO pag);
    Todo selectTodoById(Long todo_id);
    int updateTodo(Todo todo);
    int deleteTodo(Long todo_id);

    // @Param 명시 : XML에서 #{user_id}, #{keyword} 로 정확히 매핑되도록 파라미터 이름을 고정
    int getTotalCount(PageRequestDTO pag);
}