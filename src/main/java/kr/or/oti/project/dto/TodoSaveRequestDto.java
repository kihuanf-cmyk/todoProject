package kr.or.oti.project.dto;

import java.sql.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoSaveRequestDto {

	private String user_id;
	private String title;
	private String content;
	private Date schedule_date;
	// userId는 화면 입력값이 아니라 세션에서 꺼내 Service 호출 시 별도로 넘김
}
