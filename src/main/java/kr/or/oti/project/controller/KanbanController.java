package kr.or.oti.project.controller;

import kr.or.oti.project.dto.TodoResponseDto;
import kr.or.oti.project.security.CustomUserDetails;
import kr.or.oti.project.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class KanbanController {

    private final TodoService todoService;

    @GetMapping("/kanban")
    public String kanban(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        log.debug("칸반 보드 요청 수신");

        Long user_no = userDetails.getUser_no();
        List<TodoResponseDto> allTodos = todoService.getAllTodoByUser(user_no);

        // status별로 3개 컬럼에 나눠 담기
        List<TodoResponseDto> todoList = allTodos.stream()
                .filter(t -> "TODO".equals(t.getStatus()))
                .collect(Collectors.toList());

        List<TodoResponseDto> doingList = allTodos.stream()
                .filter(t -> "DOING".equals(t.getStatus()))
                .collect(Collectors.toList());

        List<TodoResponseDto> doneList = allTodos.stream()
                .filter(t -> "DONE".equals(t.getStatus()))
                .collect(Collectors.toList());

        model.addAttribute("todoList", todoList);
        model.addAttribute("doingList", doingList);
        model.addAttribute("doneList", doneList);

        log.debug("칸반 보드 응답 완료 - 시작전 {}건, 진행중 {}건, 완료 {}건",
                todoList.size(), doingList.size(), doneList.size());

        return "kanban";
    }
}