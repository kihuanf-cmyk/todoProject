package kr.or.oti.project.dto;

import java.sql.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoUpdateRequestDto {
	
	private Long todo_id;
	private String title;
	private String content;
	private Date schedule_date;
	private String is_checked;
}
