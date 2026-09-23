package todoProject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import kr.or.oti.project.controller.HomeController;
import kr.or.oti.project.domain.User;
import kr.or.oti.project.dto.TodoResponseDto;
import kr.or.oti.project.security.CustomUserDetails;
import kr.or.oti.project.service.TodoService;

@ExtendWith(MockitoExtension.class)
class HomeTests {

    @Mock
    private TodoService todoService;

    @Mock
    private Model model;

    @InjectMocks
    private HomeController homeController;

    @Test
    @DisplayName("홈 화면은 오늘의 일정과 최신 작업 5건을 선별하여 Model에 담는다")
    @SuppressWarnings("unchecked")
    void 홈_대시보드_일정_선별_테스트() {
        // Given
        User user = new User();
        user.setUser_no(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);
        LocalDate twoDaysAgo = today.minusDays(2);
        LocalDate threeDaysAgo = today.minusDays(3);
        LocalDate fourDaysAgo = today.minusDays(4);

        TodoResponseDto tToday = TodoResponseDto.builder().todo_id(1L).title("오늘 일정").schedule_date(Date.valueOf(today)).build();
        TodoResponseDto tTomorrow = TodoResponseDto.builder().todo_id(2L).title("내일 일정").schedule_date(Date.valueOf(tomorrow)).build();
        TodoResponseDto tYesterday = TodoResponseDto.builder().todo_id(3L).title("어제 일정").schedule_date(Date.valueOf(yesterday)).build();
        TodoResponseDto t2DaysAgo = TodoResponseDto.builder().todo_id(4L).title("2일 전").schedule_date(Date.valueOf(twoDaysAgo)).build();
        TodoResponseDto t3DaysAgo = TodoResponseDto.builder().todo_id(5L).title("3일 전").schedule_date(Date.valueOf(threeDaysAgo)).build();
        TodoResponseDto t4DaysAgo = TodoResponseDto.builder().todo_id(6L).title("4일 전").schedule_date(Date.valueOf(fourDaysAgo)).build();

        when(todoService.getAllTodoByUser(1L)).thenReturn(List.of(tToday, tTomorrow, tYesterday, t2DaysAgo, t3DaysAgo, t4DaysAgo));

        // When
        String viewName = homeController.home(userDetails, model);

        // Then
        assertThat(viewName).isEqualTo("home");

        ArgumentCaptor<List<TodoResponseDto>> todayCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<TodoResponseDto>> recentCaptor = ArgumentCaptor.forClass(List.class);

        verify(model).addAttribute(org.mockito.ArgumentMatchers.eq("todayTodos"), todayCaptor.capture());
        verify(model).addAttribute(org.mockito.ArgumentMatchers.eq("recentTodos"), recentCaptor.capture());

        // 1. 오늘의 일정 검증: 당일 날짜만 포함
        List<TodoResponseDto> todayList = todayCaptor.getValue();
        assertThat(todayList).hasSize(1);
        assertThat(todayList.get(0).getTitle()).isEqualTo("오늘 일정");

        // 2. 최근 작업 검증: 최대 5건, 최신순 정렬 (내일 -> 오늘 -> 어제 -> 2일전 -> 3일전)
        List<TodoResponseDto> recentList = recentCaptor.getValue();
        assertThat(recentList).hasSize(5);
        assertThat(recentList.get(0).getTitle()).isEqualTo("내일 일정");
        assertThat(recentList.get(1).getTitle()).isEqualTo("오늘 일정");
        assertThat(recentList.get(2).getTitle()).isEqualTo("어제 일정");
    }
}

