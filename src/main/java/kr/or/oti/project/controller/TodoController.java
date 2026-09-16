package kr.or.oti.project.controller;

import javax.validation.Valid;

import kr.or.oti.project.dto.TodoResponseDto;
import kr.or.oti.project.dto.TodoSaveRequestDto;
import kr.or.oti.project.dto.TodoUpdateRequestDto;
import kr.or.oti.project.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/todo")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    // 목록 조회 + 검색 기능 통합
    @GetMapping("/list")
    public String list(Model model,
                       Authentication authentication,
                       @RequestParam(required = false) String keyword) {
        String user_id = authentication.getName();

        if (keyword != null && !keyword.trim().isEmpty()) {
            model.addAttribute("todoList", todoService.searchTodoByTitle(user_id, keyword));
            model.addAttribute("keyword", keyword);
        } else {
            model.addAttribute("todoList", todoService.getTodoList(user_id));
        }
        return "todo/list";
    }

    // 등록 폼 조회
    @GetMapping("/register")
    public String registerForm() {
        return "todo/register";
    }

    // 등록 (@Valid 를 통해 빈 제목, 글자 수 초과 등을 서버 단에서 검증)
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute TodoSaveRequestDto dto, Authentication authentication) {
        String user_id = authentication.getName();
        todoService.saveTodo(dto, user_id);
        return "redirect:/todo/list";
    }

    // 상세 조회 (남의 일정 조회 차단을 위해 user_id 전달)
    @GetMapping("/{todo_id}")
    public String detail(@PathVariable Long todo_id, Authentication authentication, Model model) {
        String user_id = authentication.getName();
        model.addAttribute("todo", todoService.getTodo(todo_id, user_id));
        return "todo/read";
    }

    // 수정 폼 조회 (남의 일정 수정 폼 접근 차단을 위해 user_id 전달)
    @GetMapping("/modify/{todo_id}")
    public String modifyForm(@PathVariable Long todo_id, Authentication authentication, Model model) {
        String user_id = authentication.getName();
        model.addAttribute("todo", todoService.getTodo(todo_id, user_id));
        return "todo/modify";
    }

    // 수정 (@Valid 유효성 검증 및 남의 일정 수정 방어)
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute TodoUpdateRequestDto dto, Authentication authentication) {
        String user_id = authentication.getName();
        todoService.updateTodo(dto, user_id);
        return "redirect:/todo/list";
    }

    // 삭제 (남의 일정 삭제 차단을 위해 user_id 전달)
    @PostMapping("/delete/{todo_id}")
    public String delete(@PathVariable Long todo_id, Authentication authentication) {
        String user_id = authentication.getName();
        todoService.deleteTodo(todo_id, user_id);
        return "redirect:/todo/list";
    }
}