package kr.or.oti.project.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.or.oti.project.dto.PageRequestDTO;
import kr.or.oti.project.dto.PageResponseDTO;
import kr.or.oti.project.dto.TodoResponseDto;
import kr.or.oti.project.dto.TodoSaveRequestDto;
import kr.or.oti.project.dto.TodoStatsDTO;
import kr.or.oti.project.dto.TodoUpdateRequestDto;
import kr.or.oti.project.security.CustomUserDetails;
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
                       @AuthenticationPrincipal CustomUserDetails userDetails,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "1") int page) {
        Long userNo = userDetails.getUser_no();
        String userId = userDetails.getUsername();

        PageRequestDTO pageRequest = new PageRequestDTO();
        pageRequest.setUser_no(userNo);
        pageRequest.setUser_id(userId);
        pageRequest.setKeyword(keyword);
        pageRequest.setPage(Math.max(page, 1));

        List<TodoResponseDto> todoList = todoService.getTodoList(pageRequest);
        int totalCount = todoService.getTotalCount(pageRequest);
        PageResponseDTO pageResponse = new PageResponseDTO(pageRequest, totalCount);

        log.debug("목록 조회 완료 - user_no={}, user_id={}, keyword={}, page={}, totalCount={}",
                userNo, userId, keyword, pageRequest.getPage(), totalCount);

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
                       @AuthenticationPrincipal CustomUserDetails userDetails,
                       @RequestParam(value = "file", required = false) MultipartFile file) {
        Long userNo = userDetails.getUser_no();
        log.debug("일정 등록 요청 - user_no={}, title={}, hasFile={}",
                userNo, dto.getTitle(), hasFile(file));

        todoService.saveTodo(dto, userNo, file);

        log.info("일정 등록 완료 - user_no={}, title={}, hasFile={}",
                userNo, dto.getTitle(), hasFile(file));
        return "redirect:/todo/list";
    }

    @GetMapping("/{todo_id}")
    public String detail(@PathVariable Long todo_id,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(required = false) String keyword,
                         @RequestParam(required = false, defaultValue = "/todo/list") String prev_url,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         Model model) {

        Long userNo = userDetails.getUser_no();

        log.debug("상세 조회 요청 - todo_id={}, user_no={}, page={}, keyword={}, prev_url={}", 
                todo_id, userNo, page, keyword, prev_url);

        model.addAttribute("todo", todoService.getTodo(todo_id, userNo));
        model.addAttribute("page", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("prev_url", prev_url);

        return "todo/read";
    }
    
    @GetMapping("/modify/{todo_id}")
    public String modifyForm(@PathVariable Long todo_id,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false, defaultValue = "/todo/list") String prev_url,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model) {

        Long userNo = userDetails.getUser_no();

        log.debug("수정 화면 요청 - todo_id={}, user_no={}, page={}, keyword={}, prev_url={}",
                todo_id, userNo, page, keyword, prev_url);

        model.addAttribute("todo", todoService.getTodo(todo_id, userNo));
        model.addAttribute("page", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("prev_url", prev_url);

        return "todo/modify";
    }
    
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute TodoUpdateRequestDto dto,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         RedirectAttributes redirectAttributes,
                         @RequestParam(value = "file", required = false) MultipartFile file) {

        Long userNo = userDetails.getUser_no();

        log.debug("일정 수정 요청 - user_no={}, todo_id={}, replaceFile={}, deleteFile={}, prev_url={}",
                userNo, dto.getTodo_id(), hasFile(file), dto.isDeleteFile(), dto.getPrev_url());

        todoService.updateTodo(dto, userNo, file);

        log.info("일정 수정 완료 - user_no={}, todo_id={}", userNo, dto.getTodo_id());

        redirectAttributes.addAttribute("page", dto.getPage());

        if (dto.getPrev_url() != null && !dto.getPrev_url().trim().isEmpty()) {
            return "redirect:" + dto.getPrev_url();
        }

        return "redirect:/todo/list";
    }
    
    @PostMapping("/delete/{todo_id}")
    public String delete(@PathVariable Long todo_id,
                         @RequestParam(required = false, defaultValue = "/todo/list") String prev_url,
                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userNo = userDetails.getUser_no();
        log.debug("일정 삭제 요청 - todo_id={}, user_no={}, prev_url={}", todo_id, userNo, prev_url);

        todoService.deleteTodo(todo_id, userNo);
        log.info("일정 삭제 완료 - user_no={}, todo_id={}", userNo, todo_id);

        return "redirect:" + prev_url;
    }
    
    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }
    
    @GetMapping("/stats")
    public String getTodoStats(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long user_no = userDetails.getUser_no();

        TodoStatsDTO stats = todoService.getTodoStats(user_no);

        model.addAttribute("stats", stats);
        log.debug("통계 조회 완료 - user_no={}, total={}, completionRate={}",
                user_no, stats.getTotal_count(), stats.getCompletion_rate());

        return "todo/stats";
    }
    
    // Kanban 카드 Drag&Drop 시 status만 변경하는 AJAX 전용 엔드포인트
    @PostMapping("/updateStatus")
    @ResponseBody
    public ResponseEntity<Void> updateStatus(@RequestParam Long todo_id,
                                              @RequestParam String status,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userNo = userDetails.getUser_no();
        log.debug("AJAX 일정 상태 변경 요청 - todo_id={}, status={}, user_no={}", todo_id, status, userNo);

        todoService.updateStatus(todo_id, status, userNo);
        log.info("AJAX 일정 상태 변경 완료 - todo_id={}, status={}, user_no={}", todo_id, status, userNo);

        return ResponseEntity.ok().build();
    }
}
