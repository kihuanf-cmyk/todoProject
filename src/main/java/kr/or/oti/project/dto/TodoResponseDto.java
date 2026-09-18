package kr.or.oti.project.dto;

import java.sql.Date;
import java.util.List;

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
	private List<kr.or.oti.project.domain.TodoFile> files; // 첨부파일 목록 추가

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
				.build();
	}

	// 상세 조회용 - 파일 목록까지 포함해서 변환
	public static TodoResponseDto
	from(kr.or.oti.project.domain.Todo todo, List<kr.or.oti.project.domain.TodoFile> files) {
		return TodoResponseDto
				.builder()
				.todo_id(todo.getTodo_id())
				.title(todo.getTitle())
				.content(todo.getContent())
				.schedule_date(todo.getSchedule_date())
				.status(todo.getStatus())
				.files(files)
				.build();
	}
}