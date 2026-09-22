// kr.or.oti.project.service.impl 패키지에 신규 생성
package kr.or.oti.project.service.impl;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;

import kr.or.oti.project.dto.GoogleEventDto;
import kr.or.oti.project.service.CalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarServiceImpl implements CalendarService {

    // 3번 단계에서 다룬 서비스 - 로그인한 사용자의 구글 access token을 꺼내올 때 사용
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Override
    public List<GoogleEventDto> getEvents(Authentication authentication) {

        // 폼 로그인 사용자는 OAuth2AuthenticationToken이 아니므로 여기서 걸러냄
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            log.debug("폼 로그인 사용자 - 구글 캘린더 조회 생략");
            return Collections.emptyList();
        }

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(), // "google"
                oauthToken.getName());                          // 로그인 사용자 식별자(이메일)

        if (client == null || client.getAccessToken() == null) {
            log.warn("구글 access token을 찾을 수 없음 - user={}", oauthToken.getName());
            return Collections.emptyList();
        }

        String accessToken = client.getAccessToken().getTokenValue();

        try {
            Calendar calendarClient = buildCalendarClient(accessToken);

            // 지난달 1일부터 조회하도록 timeMin 조정 
            java.time.LocalDate oneMonthAgo = java.time.LocalDate.now().minusMonths(2).withDayOfMonth(2);
            DateTime timeMin = new DateTime(
                    java.util.Date.from(oneMonthAgo.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));

            Events events = calendarClient.events().list("primary")
                    .setMaxResults(100)               // 과거 일정까지 포함하니 넉넉하게 조정
                    .setTimeMin(timeMin)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            List<GoogleEventDto> result = new ArrayList<>();
            for (Event event : events.getItems()) {
                result.add(toDto(event));
            }

            log.debug("구글 캘린더 조회 완료 - user={}, count={}", oauthToken.getName(), result.size());
            return result;

        } catch (Exception e) {
            // 캘린더 조회 실패가 화면 전체를 깨뜨리지 않도록 여기서 흡수(Todo는 정상 노출되어야 함)
            log.error("구글 캘린더 조회 실패 - user={}", oauthToken.getName(), e);
            return Collections.emptyList();
        }
    }

    // GoogleCredential 자리를 GoogleCredentials + HttpCredentialsAdapter로 교체
    private Calendar buildCalendarClient(String accessToken) throws GeneralSecurityException, IOException {
        GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(accessToken, null));

        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("DayPlanner")
                .build();
    }
    
    private GoogleEventDto toDto(Event event) {
        EventDateTime start = event.getStart();
        EventDateTime end = event.getEnd();

        // date만 있고 dateTime이 없으면 종일 일정
        boolean allDay = start.getDate() != null;

        return GoogleEventDto.builder()
                .event_id(event.getId())
                .calendar_id("primary")
                .title(event.getSummary())
                .description(event.getDescription())
                .start(allDay ? start.getDate().toStringRfc3339() : start.getDateTime().toStringRfc3339())
                .end(allDay ? end.getDate().toStringRfc3339() : end.getDateTime().toStringRfc3339())
                .all_day(allDay)
                .build();
    }
}