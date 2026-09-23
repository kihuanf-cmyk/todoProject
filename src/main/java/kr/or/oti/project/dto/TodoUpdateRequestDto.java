package kr.or.oti.project.dto;

import java.sql.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoUpdateRequestDto {

    @NotNull(message = "식별번호는 필수입니다.")
    private Long todo_id;

    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(max = 50, message = "제목은 최대 50자까지 입력 가능합니다.")
    private String title;

    @NotBlank(message = "내용은 필수 입력 항목입니다.")
    @Size(max = 500, message = "내용은 최대 500자까지 입력 가능합니다.")
    private String content;

    @NotNull(message = "날짜는 필수 입력 항목입니다.")
    private Date schedule_date;

    @Pattern(regexp = "^(TODO|DOING|DONE)$", message = "상태값은 TODO, DOING, DONE 중 하나여야 합니다.")
    private String status;

    private String prev_url;
    private boolean deleteFile = false;
    private int page = 1;
    private String keyword;
}
