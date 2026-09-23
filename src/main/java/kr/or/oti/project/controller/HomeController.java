package kr.or.oti.project.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import kr.or.oti.project.dto.TodoResponseDto;
import kr.or.oti.project.security.CustomUserDetails;
import kr.or.oti.project.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final TodoService todoService;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        log.debug("홈 화면 요청 수신");

        // authentication.getName()은 user_id(String)라 서비스 시그니처(Long user_no)와 안 맞아서 교체
        Long user_no = userDetails.getUser_no();

        // TodoService.getAllTodoByUser(Long user_no)는 List<TodoResponseDto> 반환
        List<TodoResponseDto> allTodos = todoService.getAllTodoByUser(user_no);
        
        
        // 오늘의 일정: schedule_date == 오늘
        // java.sql.Date는 LocalDate와 직접 비교 불가 → toLocalDate()로 변환 후 비교
        List<TodoResponseDto> todayTodos = allTodos.stream()
                .filter(todo -> todo.getSchedule_date().toLocalDate().equals(LocalDate.now()))
                .collect(Collectors.toList());
        
        
        // 최근 작업: 최신순 상위 5건 (기간 제한 없음)
        // java.sql.Date는 Comparable<Date>를 구현하므로 compareTo는 그대로 사용 가능
        List<TodoResponseDto> recentTodos = allTodos.stream()
                .sorted((a, b) -> b.getSchedule_date().compareTo(a.getSchedule_date()))
                .limit(5)
                .collect(Collectors.toList());
        
        model.addAttribute("todayTodos", todayTodos);
        model.addAttribute("recentTodos", recentTodos);

        log.debug("홈 화면 응답 완료 - 오늘 일정 {}건, 최근 작업 {}건", todayTodos.size(), recentTodos.size());

        return "home";
    }
}