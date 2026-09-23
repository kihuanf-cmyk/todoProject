// src/test/java/todoProject/CalendarTests.java
package todoProject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import kr.or.oti.project.domain.Todo;
import kr.or.oti.project.dto.GoogleEventDto;
import kr.or.oti.project.dto.TodoResponseDto;
import kr.or.oti.project.mapper.TodoMapper;
import kr.or.oti.project.service.impl.CalendarServiceImpl;
import kr.or.oti.project.service.impl.TodoServiceImpl;

// 테스트 작성일: 2026-09-22
// 테스트 순서: DTO -> CalendarService -> TodoService(전체 조회)
@ExtendWith(MockitoExtension.class)
class CalendarTests {

    @Nested
    @DisplayName("GoogleEventDto 단위 테스트")
    class GoogleEventDto도메인테스트 {

        @Test
        @DisplayName("GoogleEventDto는 빌더로 입력한 값을 그대로 보관한다")
        void GoogleEventDto는입력한값을보관한다() {
            GoogleEventDto dto = GoogleEventDto.builder()
                    .event_id("evt1")
                    .calendar_id("primary")
                    .title("회의")
                    .description("주간 회의")
                    .start("2026-09-22T10:00:00+09:00")
                    .end("2026-09-22T11:00:00+09:00")
                    .all_day(false)
                    .build();

            assertThat(dto.getEvent_id()).isEqualTo("evt1");
            assertThat(dto.getCalendar_id()).isEqualTo("primary");
            assertThat(dto.getTitle()).isEqualTo("회의");
            assertThat(dto.getDescription()).isEqualTo("주간 회의");
            assertThat(dto.getStart()).isEqualTo("2026-09-22T10:00:00+09:00");
            assertThat(dto.getEnd()).isEqualTo("2026-09-22T11:00:00+09:00");
            assertThat(dto.isAll_day()).isFalse();
        }
    }

    @Nested
    @DisplayName("CalendarService 테스트")
    class CalendarService테스트 {

        @Mock
        private OAuth2AuthorizedClientService authorizedClientService;

        @InjectMocks
        private CalendarServiceImpl calendarService;

        @Test
        @DisplayName("폼 로그인 사용자는 구글 캘린더 조회 없이 빈 리스트를 반환한다")
        void 폼로그인사용자는빈리스트를반환한다() {
            Authentication formLoginAuth = mock(Authentication.class); // OAuth2AuthenticationToken이 아닌 일반 Authentication

            List<GoogleEventDto> result = calendarService.getEvents(formLoginAuth);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("구글 로그인 사용자라도 access token을 찾을 수 없으면 빈 리스트를 반환한다")
        void 토큰이없으면빈리스트를반환한다() {
            OAuth2AuthenticationToken oauthToken = mock(OAuth2AuthenticationToken.class);
            when(oauthToken.getAuthorizedClientRegistrationId()).thenReturn("google");
            when(oauthToken.getName()).thenReturn("user@gmail.com");
            when(authorizedClientService.loadAuthorizedClient("google", "user@gmail.com"))
                    .thenReturn(null); // 토큰 없음 상황 재현

            List<GoogleEventDto> result = calendarService.getEvents(oauthToken);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("시간 지정 일정(Event)을 GoogleEventDto로 변환하면 all_day가 false다")
        void 시간지정일정은allDay가false다() {
            Event event = new Event();
            event.setId("evt1");
            event.setSummary("팀 회의");
            event.setDescription("주간 팀 회의");
            event.setStart(new EventDateTime().setDateTime(new DateTime("2026-09-22T10:00:00+09:00")));
            event.setEnd(new EventDateTime().setDateTime(new DateTime("2026-09-22T11:00:00+09:00")));

            GoogleEventDto dto = calendarService.toDto(event);

            assertThat(dto.getEvent_id()).isEqualTo("evt1");
            assertThat(dto.getTitle()).isEqualTo("팀 회의");
            assertThat(dto.isAll_day()).isFalse();
        }

        @Test
        @DisplayName("종일 일정(Event)을 GoogleEventDto로 변환하면 all_day가 true다")
        void 종일일정은allDay가true다() {
            Event event = new Event();
            event.setId("evt2");
            event.setSummary("휴가");
            event.setStart(new EventDateTime().setDate(new DateTime("2026-09-25")));
            event.setEnd(new EventDateTime().setDate(new DateTime("2026-09-26")));

            GoogleEventDto dto = calendarService.toDto(event);

            assertThat(dto.getEvent_id()).isEqualTo("evt2");
            assertThat(dto.isAll_day()).isTrue();
        }
    }

    @Nested
    @DisplayName("TodoService 전체 조회(getAllTodoByUser) 테스트")
    class TodoService전체조회테스트 {

        @Mock
        private TodoMapper todoMapper;

        @InjectMocks
        private TodoServiceImpl todoService;

        @Test
        @DisplayName("전체 조회는 페이징 없이 해당 사용자의 모든 일정을 반환한다")
        void 전체조회는사용자의모든일정을반환한다() {
            Todo todo1 = new Todo();
            todo1.setTodo_id(1L);
            todo1.setUser_no(4L);
            todo1.setTitle("초코바나나");

            Todo todo2 = new Todo();
            todo2.setTodo_id(2L);
            todo2.setUser_no(4L);
            todo2.setTitle("우유 사기");

            when(todoMapper.selectAllTodoByUser(4L)).thenReturn(List.of(todo1, todo2));

            List<TodoResponseDto> result = todoService.getAllTodoByUser(4L);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getTitle()).isEqualTo("초코바나나");
            verify(todoMapper).selectAllTodoByUser(4L);
        }

        @Test
        @DisplayName("등록된 일정이 없으면 빈 리스트를 반환한다")
        void 일정이없으면빈리스트를반환한다() {
            when(todoMapper.selectAllTodoByUser(4L)).thenReturn(Collections.emptyList());

            List<TodoResponseDto> result = todoService.getAllTodoByUser(4L);

            assertThat(result).isEmpty();
        }
    }
}