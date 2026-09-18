package kr.or.oti.project.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoFile {
    private Long file_id;
    private Long todo_id;
    private String file_url;
    private String file_name;
}