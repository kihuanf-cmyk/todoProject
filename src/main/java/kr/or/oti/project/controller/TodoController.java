package kr.or.oti.project.controller;

import java.util.List;

import javax.validation.Valid;

import kr.or.oti.project.dto.PageRequestDTO;
import kr.or.oti.project.dto.PageResponseDTO;
import kr.or.oti.project.dto.TodoResponseDto;
import kr.or.oti.project.dto.TodoSaveRequestDto;
import kr.or.oti.project.dto.TodoUpdateRequestDto;
import kr.or.oti.project.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/todo")
@RequiredArgsConstructor
public class TodoController {

	private final TodoService todoService;

	// 목록 조회 + 검색 기능 통합
	@GetMapping("/list")
	public String list(Model model,
	                   Authentication authentication,
	                   @RequestParam(required = false) String keyword,
	                   @RequestParam(defaultValue = "1") int page) {        // ← 페이지 파라미터, 없으면 1페이지
	    String user_id = authentication.getName();

	    PageRequestDTO pag = new PageRequestDTO();
	    pag.setUser_id(user_id);
	    pag.setKeyword(keyword);
	    pag.setPage(page);
	    // amount는 기본값 10 그대로 사용

	    List<TodoResponseDto> todoList = todoService.getTodoList(pag);
	    int totalCount = todoService.getTotalCount(pag);
	    PageResponseDTO pageResponseDTO = new PageResponseDTO(pag, totalCount);

	    log.debug("목록 조회 - user_id={}, keyword={}, page={}, totalCount={}", user_id, keyword, page, totalCount);

	    model.addAttribute("todoList", todoList);
	    model.addAttribute("keyword", keyword);
	    model.addAttribute("pageResponseDTO", pageResponseDTO);
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
		log.info("일정 등록 완료 - user_id={}, title={}", user_id, dto.getTitle());
		todoService.saveTodo(dto, user_id);
		return "redirect:/todo/list";
	}

	// 상세 조회 (남의 일정 조회 차단을 위해 user_id 전달)
	@GetMapping("/{todo_id}")
	public String detail(@PathVariable Long todo_id, Authentication authentication, Model model) {
		String user_id = authentication.getName();
		log.debug("상세 조회 요청 - todo_id={}, user_id={}", todo_id, user_id);
		model.addAttribute("todo", todoService.getTodo(todo_id, user_id));
		return "todo/read";
	}

	// 수정 폼 조회 (남의 일정 수정 폼 접근 차단을 위해 user_id 전달)
	@GetMapping("/modify/{todo_id}")
	public String modifyForm(@PathVariable Long todo_id, Authentication authentication, Model model) {
		String user_id = authentication.getName();
		log.debug("수정 폼 조회 - todo_id={}, user_id={}", todo_id, user_id);
		model.addAttribute("todo", todoService.getTodo(todo_id, user_id));
		return "todo/modify";
	}

	// 수정 (@Valid 유효성 검증 및 남의 일정 수정 방어)
	@PostMapping("/update")
	public String update(@Valid @ModelAttribute TodoUpdateRequestDto dto, Authentication authentication) {
		String user_id = authentication.getName();
		todoService.updateTodo(dto, user_id);
		log.info("일정 수정 완료 - user_id={}, todo_id={}", user_id, dto.getTodo_id()); // ← 추가 (필드명은 실제 DTO 확인)
		return "redirect:/todo/list";
	}

	// 삭제 (남의 일정 삭제 차단을 위해 user_id 전달)
	@PostMapping("/delete/{todo_id}")
	public String delete(@PathVariable Long todo_id, Authentication authentication) {
		String user_id = authentication.getName();
		log.debug("삭제 요청 - todo_id={}, user_id={}", todo_id, user_id);
		todoService.deleteTodo(todo_id, user_id);
		return "redirect:/todo/list";
	}
}