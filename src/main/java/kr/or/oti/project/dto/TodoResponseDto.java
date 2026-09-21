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
	private String status;
	private String file_url;
	private String file_name;

	// 목록 조회용 - 파일 목록 없이 변환 (files는 null)
	public static TodoResponseDto 
	from(kr.or.oti.project.domain.Todo todo) {
		return TodoResponseDto
				.builder()
				.todo_id(todo.getTodo_id())
				.title(todo.getTitle())
				.content(todo.getContent())
				.schedule_date(todo.getSchedule_date())
				.status(todo.getStatus())
				.file_url(todo.getFile_url())
				.file_name(todo.getFile_name())
				.build();
	}
}
