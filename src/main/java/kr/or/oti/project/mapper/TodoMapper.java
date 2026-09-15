package kr.or.oti.project.mapper;

import kr.or.oti.project.domain.Todo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TodoMapper {
    int insertTodo(Todo todo);
    List<Todo> selectTodoListByUser(String userId);
    Todo selectTodoById(Long todoId);
    int updateTodo(Todo todo);
    int deleteTodo(Long todoId);
    List<Todo> searchTodoByTitle(String userId, String keyword);
}