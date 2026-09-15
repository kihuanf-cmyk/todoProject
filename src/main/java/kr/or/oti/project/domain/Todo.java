package kr.or.oti.project.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Todo {
    private Long todoId;        // NUMBER -> Long
    private String userId;
    private String title;
    private String content;
    private Date scheduleDate;
    private String isChecked;
    private String fileUrl;
}