package kr.or.oti.project.mapper;

import kr.or.oti.project.domain.Todo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TodoMapper {
    int insertTodo(Todo todo);
    List<Todo> selectTodoListByUser(String user_id);
    Todo selectTodoById(Long todo_id);
    int updateTodo(Todo todo);
    int deleteTodo(Long todo_id);

    // @Param 명시 : XML에서 #{user_id}, #{keyword} 로 정확히 매핑되도록 파라미터 이름을 고정
    List<Todo> searchTodoByTitle(@Param("user_id") String user_id,
                                 @Param("keyword") String keyword);
}