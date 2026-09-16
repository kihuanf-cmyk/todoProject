package kr.or.oti.project.controller;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * ✅ 전역 예외 처리 클래스
 *
 * @ControllerAdvice : 모든 컨트롤러에서 발생하는 예외를 한 곳에서 모아서 처리합니다.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 1. 입력값 유효성 검증 실패 처리 (빈 제목, 글자 수 초과, 날짜 누락 등)
     *    DTO의 @Valid 검증에서 에러가 발생했을 때 호출됩니다.
     */
    @ExceptionHandler(BindException.class)
    public String handleBindException(BindException e, Model model) {
        model.addAttribute("errorCode", "400");
        model.addAttribute("errorTitle", "입력값이 올바르지 않습니다");

        // DTO 에 작성한 첫 번째 에러 메시지 추출 (예: "일정 제목은 필수 입력 항목입니다.")
        String errorMessage = "입력값을 다시 확인해 주세요.";
        if (e.getBindingResult().hasErrors()) {
            errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        }
        model.addAttribute("errorMessage", errorMessage);
        return "error/error";
    }

    /**
     * 2. 권한 거부 예외 처리 (남의 일정 조회/수정/삭제 시도 등)
     *    Service 에서 소유권 불일치 시 throw new AccessDeniedException(...) 을 던질 때 처리합니다.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException e, Model model) {
        model.addAttribute("errorCode", "403");
        model.addAttribute("errorTitle", "접근 권한이 없습니다");
        model.addAttribute("errorMessage", e.getMessage() != null ? e.getMessage() : "해당 데이터에 접근할 권한이 없습니다.");
        return "error/error";
    }

    /**
     * 3. 존재하지 않는 일정 요청, 수정/삭제 대상 없음 등
     *    Service 에서 throw new IllegalArgumentException(...) 으로 명확하게 던진 경우를 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException e, Model model) {
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorTitle", "항목을 찾을 수 없습니다");
        model.addAttribute("errorMessage", e.getMessage());
        return "error/error";
    }

    /**
     * 4. DB 연결 오류, 예상하지 못한 모든 서버 오류
     *    가장 넓은 범위의 예외로 앞에서 처리되지 않은 모든 오류를 여기서 처리
     */
    @ExceptionHandler(Exception.class)
    public String handleAllException(Exception e, Model model) {
        model.addAttribute("errorCode", "500");
        model.addAttribute("errorTitle", "서버 오류가 발생했습니다");
        model.addAttribute("errorMessage", "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        return "error/error";
    }
}
