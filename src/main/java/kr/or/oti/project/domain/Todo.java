package kr.or.oti.project.domain;

import java.sql.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Todo {
    private Long user_no;        // NUMBER -> Long
    private Long todo_id;
    private String user_id;
    private String title;
    private String content;
    private Date schedule_date;
    private String status;
    private String file_url;
    private String file_name;
}
