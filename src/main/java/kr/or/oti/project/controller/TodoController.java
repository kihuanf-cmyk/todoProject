package kr.or.oti.project.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.or.oti.project.dto.PageRequestDTO;
import kr.or.oti.project.dto.PageResponseDTO;
import kr.or.oti.project.dto.TodoResponseDto;
import kr.or.oti.project.dto.TodoSaveRequestDto;
import kr.or.oti.project.dto.TodoUpdateRequestDto;
import kr.or.oti.project.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/todo")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @GetMapping("/list")
    public String list(Model model,
                       Authentication authentication,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "1") int page) {
        String userId = authentication.getName();

        PageRequestDTO pageRequest = new PageRequestDTO();
        pageRequest.setUser_id(userId);
        pageRequest.setKeyword(keyword);
        pageRequest.setPage(Math.max(page, 1));

        List<TodoResponseDto> todoList = todoService.getTodoList(pageRequest);
        int totalCount = todoService.getTotalCount(pageRequest);
        PageResponseDTO pageResponse = new PageResponseDTO(pageRequest, totalCount);

        log.debug("목록 조회 완료 - user_id={}, keyword={}, page={}, totalCount={}",
                userId, keyword, pageRequest.getPage(), totalCount);

        model.addAttribute("todoList", todoList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("pageResponseDTO", pageResponse);
        return "todo/list";
    }

    @GetMapping("/register")
    public String registerForm() {
        log.debug("일정 등록 화면 요청");
        return "todo/register";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute TodoSaveRequestDto dto,
                       Authentication authentication,
                       @RequestParam(value = "file", required = false) MultipartFile file) {
        String userId = authentication.getName();
        log.debug("일정 등록 요청 - user_id={}, title={}, hasFile={}",
                userId, dto.getTitle(), hasFile(file));

        todoService.saveTodo(dto, userId, file);

        log.info("일정 등록 완료 - user_id={}, title={}, hasFile={}",
                userId, dto.getTitle(), hasFile(file));
        return "redirect:/todo/list";
    }

    @GetMapping("/{todo_id}")
    public String detail(@PathVariable Long todo_id,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(required = false) String keyword,
                          Authentication authentication,
                          Model model) {
        String userId = authentication.getName();
        log.debug("상세 조회 요청 - todo_id={}, user_id={}, page={}, keyword={}",
                todo_id, userId, page, keyword);

        model.addAttribute("todo", todoService.getTodo(todo_id, userId));
        model.addAttribute("page", page);
        model.addAttribute("keyword", keyword);
        return "todo/read";
    }

    @GetMapping("/modify/{todo_id}")
    public String modifyForm(@PathVariable Long todo_id,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(required = false) String keyword,
                              Authentication authentication,
                              Model model) {
        String userId = authentication.getName();
        log.debug("수정 화면 요청 - todo_id={}, user_id={}, page={}, keyword={}",
                todo_id, userId, page, keyword);

        model.addAttribute("todo", todoService.getTodo(todo_id, userId));
        model.addAttribute("page", page);
        model.addAttribute("keyword", keyword);
        return "todo/modify";
    }

    @PostMapping("/update")
    public String update(@Valid @ModelAttribute TodoUpdateRequestDto dto,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes,
                          @RequestParam(value = "file", required = false) MultipartFile file) {
        String userId = authentication.getName();
        log.debug("일정 수정 요청 - user_id={}, todo_id={}, replaceFile={}, deleteFile={}",
                userId, dto.getTodo_id(), hasFile(file), dto.isDeleteFile());

        todoService.updateTodo(dto, userId, file);

        log.info("일정 수정 완료 - user_id={}, todo_id={}, replaceFile={}, deleteFile={}",
                userId, dto.getTodo_id(), hasFile(file), dto.isDeleteFile());

        redirectAttributes.addAttribute("page", dto.getPage());
        if (dto.getKeyword() != null && !dto.getKeyword().isEmpty()) {
            redirectAttributes.addAttribute("keyword", dto.getKeyword());
        }
        return "redirect:/todo/list";
    }

    @PostMapping("/delete/{todo_id}")
    public String delete(@PathVariable Long todo_id, Authentication authentication) {
        String userId = authentication.getName();
        log.debug("일정 삭제 요청 - todo_id={}, user_id={}", todo_id, userId);

        todoService.deleteTodo(todo_id, userId);

        log.info("일정 삭제 완료 - user_id={}, todo_id={}", userId, todo_id);
        return "redirect:/todo/list";
    }

    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }
}

