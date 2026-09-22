package kr.or.oti.project.domain;

import java.sql.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Todo {
    private Long todo_id;        // NUMBER -> Long (PK)
    private Long user_no;        // NUMBER -> Long (FK: TODO_USER.USER_NO)
    private String title;
    private String content;
    private Date schedule_date;
    private String status;
    private String file_url;
    private String file_name;
}
