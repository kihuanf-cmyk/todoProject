// 모든 AJAX 요청에 CSRF 토큰을 헤더로 자동 첨부 (CSRF가 꺼져있지 않으므로 필수)
$.ajaxSetup({
    beforeSend: function (xhr) {
        const token = $("meta[name='_csrf']").attr("content");
        const header = $("meta[name='_csrf_header']").attr("content");
        if (token && header) {
            xhr.setRequestHeader(header, token);
        }
    }
});

$(function () {

    let draggedCard = null;

    // 드래그 시작
    $(".kanban-card").on("dragstart", function (e) {
        draggedCard = this;
        $(this).addClass("dragging");
    });

    // 드래그 종료
    $(".kanban-card").on("dragend", function () {
        $(this).removeClass("dragging");
        $(".kanban-column").removeClass("drag-over");
    });

    // 컬럼 위로 드래그
    $(".kanban-column").on("dragover", function (e) {
        e.preventDefault();
        $(this).addClass("drag-over");
    });

    // 컬럼에서 드래그가 빠짐
    $(".kanban-column").on("dragleave", function () {
        $(this).removeClass("drag-over");
    });

    // Drop
    $(".kanban-column").on("drop", function (e) {
        e.preventDefault();
        $(this).removeClass("drag-over");

        if (!draggedCard) {
            return;
        }

        const $card = $(draggedCard);
        const todoId = $card.attr("data-id");
        const newStatus = $(this).attr("data-status");
        const $targetColumn = $(this);

        // 같은 컬럼이면 이동하지 않음
        if ($card.parent()[0] === $targetColumn[0]) {
            draggedCard = null;
            return;
        }

        // 서버에 상태 변경 요청
        $.ajax({
            url: "/todo/updateStatus",
            type: "POST",
            data: {
                todo_id: todoId,
                status: newStatus
            },

            success: function () {
                // 화면에서 카드 이동
                $targetColumn.append($card);

                // 카드의 현재 상태도 변경
                $card.attr("data-status", newStatus);
            },

            error: function (xhr) {
                alert("상태 변경에 실패했습니다. 다시 시도해주세요.");
            },
            complete: function () {
                draggedCard = null;
            }
        });
    });

    // 카드 클릭
    $(".kanban-card").on("click", function () {

        // 드래그 후 클릭 방지
        if ($(this).hasClass("dragging")) {
            return;
        }

        const todoId = $(this).attr("data-id");
        location.href = "/todo/" + todoId + "?prev_url=/kanban";
    });

});

