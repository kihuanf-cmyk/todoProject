package kr.or.oti.project.domain;

import java.sql.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Todo {
    private Long todo_id;        // NUMBER -> Long
    private String user_id;
    private String title;
    private String content;
    private Date schedule_date;
    private String status;
}