// kr.or.oti.project.service 패키지에 신규 생성
package kr.or.oti.project.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import kr.or.oti.project.dto.GoogleEventDto;

public interface CalendarService {

    // 폼 로그인 사용자는 구글 토큰이 없으므로 빈 리스트를 반환함(예외 아님)
    List<GoogleEventDto> getEvents(Authentication authentication);
}