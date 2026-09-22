package kr.or.oti.project.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageRequestDTO {

    private Long user_no;        // 로그인한 사용자의 고유 식별번호 (TODO_USER.USER_NO)
    private String user_id;      // 사용자 아이디
    private String keyword;      // 검색어 (없으면 null 또는 빈 문자열)
    private int page = 1;        // 현재 페이지 (기본값 1페이지)
    private int amount = 10;     // 한 페이지에 보여줄 개수 (기본값 10개)

    // OFFSET 값 계산: 1페이지면 0, 2페이지면 10, 3페이지면 20...
    public int getOffset() {
        return (this.page - 1) * this.amount;
    }
}