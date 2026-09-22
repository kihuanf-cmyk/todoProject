package kr.or.oti.project.dto;

import lombok.Data;

@Data
public class TodoStatsDTO {

    private long total_count;      // 전체 Todo 수
    private long todo_count;       // 시작전(TODO) 개수
    private long doing_count;      // 진행중(DOING) 개수
    private long done_count;       // 완료(DONE) 개수
    private double completion_rate; // 완료율(%) - DB 저장 X, 계산해서 채움
}