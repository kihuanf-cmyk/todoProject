package kr.or.oti.project.mapper;

import kr.or.oti.project.domain.Todo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TodoMapper {
//	@Param("user_id") String user_id; 
//	@Param("keyword") String keyword;
    int insertTodo(Todo todo);
    List<Todo> selectTodoListByUser(String user_id);
    Todo selectTodoById(Long todo_id);
    int updateTodo(Todo todo);
    int deleteTodo(Long todo_id);
    List<Todo> searchTodoByTitle(String user_id, String keyword);
}