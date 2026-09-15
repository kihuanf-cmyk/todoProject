package kr.or.oti.project.controller;

import kr.or.oti.project.dto.TodoResponseDto;
import kr.or.oti.project.dto.TodoSaveRequestDto;
import kr.or.oti.project.dto.TodoUpdateRequestDto;
import kr.or.oti.project.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/todo")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    // 목록 조회
    @GetMapping("/list")
    public String list(Model model, HttpSession session) {
        if (session.getAttribute("user_id") == null) {
            session.setAttribute("user_id", "test01"); // 로그인 붙기 전 임시 테스트용
        }
        String user_id = (String) session.getAttribute("user_id");
        model.addAttribute("todoList", todoService.getTodoList(user_id));
        return "todo/list";
    }

    // 등록 폼 조회 - register.html을 보여주는 GET 메서드 (새로 추가)
    @GetMapping("/register")
    public String registerForm() {
        return "todo/register";
    }

    // 등록
    @PostMapping("/save")
    public String save(@ModelAttribute TodoSaveRequestDto dto, HttpSession session) {
        String user_id = (String) session.getAttribute("user_id");
        todoService.saveTodo(dto, user_id);
        return "redirect:/todo/list";
    }

    // 상세 조회 - read.html로 뷰 이름 변경
    @GetMapping("/{todo_id}")
    public String detail(@PathVariable Long todo_id, Model model) {
        model.addAttribute("todo", todoService.getTodo(todo_id));
        return "todo/read";
    }

    // 수정 폼 조회 - modify.html에 기존 값 채워서 보여주는 GET 메서드
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