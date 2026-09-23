$(function () {
    $(".todo-card").on("click", function () {
        const todoId = $(this).attr("data-id");
        location.href = "/todo/" + todoId + "?prev_url=/";
    });
});

