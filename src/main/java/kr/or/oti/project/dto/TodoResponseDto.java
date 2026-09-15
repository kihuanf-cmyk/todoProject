package kr.or.oti.project.dto;

import java.sql.Date;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TodoResponseDto {

	private Long todo_id;
	private String title;
	private String content;
	private Date schedule_date;
	private String is_checked;

	public static TodoResponseDto 
	from(kr.or.oti.project.domain.Todo todo) {
		return TodoResponseDto
				.builder()
				.todo_id(todo.getTodo_id())
				.title(todo.getTitle())
				.content(todo.getContent())
				.schedule_date(todo.getSchedule_date())
				.is_checked(todo.getIs_checked())
				.build();
	}
}