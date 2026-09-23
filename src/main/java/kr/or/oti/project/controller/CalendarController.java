// kr.or.oti.project.controller 패키지에 신규 생성
package kr.or.oti.project.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.or.oti.project.dto.GoogleEventDto;
import kr.or.oti.project.dto.TodoResponseDto;
import kr.or.oti.project.security.CustomUserDetails;
import kr.or.oti.project.service.CalendarService;
import kr.or.oti.project.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;
    private final TodoService todoService;

    @GetMapping
    public String calendar(@AuthenticationPrincipal CustomUserDetails userDetails,
                            Authentication authentication,
                            Model model) {
        Long userNo = userDetails.getUser_no();
        log.debug("캘린더 화면 요청 수신 - user_no={}", userNo);

        // Todo는 페이징 없이 전체 조회 - 캘린더에 schedule_date 기준으로 전부 표시하기 위함
        // (기존 getTodoList(PageRequestDTO)는 페이징 전용이라, 여기선 전체 조회용 메서드가 필요함)
        List<TodoResponseDto> todoList = todoService.getAllTodoByUser(userNo);

        // 구글 로그인 사용자가 아니면 CalendarServiceImpl 내부에서 빈 리스트 반환(예외 아님)
        List<GoogleEventDto> googleEvents = calendarService.getEvents(authentication);

        log.debug("캘린더 화면 조회 - user_no={}, todoCount={}, googleEventCount={}",
                userNo, todoList.size(), googleEvents.size());

        model.addAttribute("todoList", todoList);
        model.addAttribute("googleEvents", googleEvents);

        return "calendar/calendar";
    }
}