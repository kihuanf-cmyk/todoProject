package kr.or.oti.project.dto;

import java.sql.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoSaveRequestDto {

    private String user_id;

    // 빈 제목 방지 및 글자 수 제한 (DB 컬럼: NVARCHAR2(50))
    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(max = 50, message = "제목은 최대 50자까지 입력 가능합니다.")
    private String title;

    // 내용 글자 수 제한 (DB 컬럼: NVARCHAR2(2000))
    @Size(max = 2000, message = "내용은 최대 2000자까지 입력 가능합니다.")
    private String content;

    // 날짜 필수 검증
    @NotNull(message = "날짜는 필수 입력 항목입니다.")
    private Date schedule_date;
}
