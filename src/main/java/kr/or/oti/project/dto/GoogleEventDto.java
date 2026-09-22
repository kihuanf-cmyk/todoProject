package kr.or.oti.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleEventDto {
    private String event_id;
    private String calendar_id;
    private String title;
    private String description;
    private String start;      // ISO 8601 문자열 (종일 일정: yyyy-MM-dd, 시간 일정: yyyy-MM-dd'T'HH:mm:ssXXX)
    private String end;
    private boolean all_day;   // true면 date만 있는 종일 일정, false면 dateTime이 있는 시간 지정 일정
}