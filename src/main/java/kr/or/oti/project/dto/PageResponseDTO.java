package kr.or.oti.project.dto;

import lombok.Getter;

@Getter
public class PageResponseDTO {

    private int startPage;    // 화면에 보일 페이지 블록의 시작 번호 (예: 1)
    private int endPage;      // 화면에 보일 페이지 블록의 끝 번호 (예: 10)
    private boolean prev;     // "이전" 버튼 표시 여부
    private boolean next;     // "다음" 버튼 표시 여부
    private int totalCount;   // 전체 게시글(일정) 개수
    private PageRequestDTO pag;     // 현재 조회 조건 (페이지 이동 링크 만들 때 필요)

    public PageResponseDTO(PageRequestDTO pag, int totalCount) {
        this.pag = pag;
        this.totalCount = totalCount;

        // 한 블록에 페이지 번호 10개씩 보여주는 방식
        this.endPage = (int) (Math.ceil(pag.getPage() / 10.0)) * 10;
        this.startPage = this.endPage - 9;

        // 전체 페이지 수를 넘어가지 않도록 끝 페이지 보정
        int realEnd = (int) (Math.ceil(totalCount / (double) pag.getAmount()));
        if (realEnd < this.endPage) {
            this.endPage = realEnd == 0 ? 1 : realEnd;
        }

        this.prev = this.startPage > 1;
        this.next = this.endPage * pag.getAmount() < totalCount;
    }
}