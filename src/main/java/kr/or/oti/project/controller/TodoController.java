package kr.or.oti.project.controller;

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

    // 목록 조회
    @GetMapping("/list")
    public String list(Model model, Authentication authentication) {
        String user_id = authentication.getName(); // 현재 로그인한 사용자의 아이디
        model.addAttribute("todoList", todoService.getTodoList(user_id));
        return "todo/list";
    }

    // 등록 폼 조회
    @GetMapping("/register")
    public String registerForm() {
        return "todo/register";
    }

    // 등록
    @PostMapping("/save")
    public String save(@ModelAttribute TodoSaveRequestDto dto, Authentication authentication) {
        String user_id = authentication.getName();
        todoService.saveTodo(dto, user_id);
        return "redirect:/todo/list";
    }

    // 상세 조회
    @GetMapping("/{todo_id}")
    public String detail(@PathVariable Long todo_id, Model model) {
        model.addAttribute("todo", todoService.getTodo(todo_id));
        return "todo/read";
    }

    // 수정 폼 조회
    @GetMapping("/modify/{todo_id}")
    public String modifyForm(@PathVariable Long todo_id, Model model) {
        model.addAttribute("todo", todoService.getTodo(todo_id));
        return "todo/modify";
    }

    // 수정
    @PostMapping("/update")
    public String update(@ModelAttribute TodoUpdateRequestDto dto) {
        todoService.updateTodo(dto);
        return "redirect:/todo/list";
    }

    // 삭제
    @PostMapping("/delete/{todo_id}")
    public String delete(@PathVariable Long todo_id) {
        todoService.deleteTodo(todo_id);
        return "redirect:/todo/list";
    }
}