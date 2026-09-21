package kr.or.oti.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.or.oti.project.domain.User;
import kr.or.oti.project.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

	private final UserService userService;

	// 회원가입 폼
	@GetMapping("/join")
	public String joinForm() {
		return "user/join";
	}

	// 회원가입 처리
	@PostMapping("/join")
	public String join(User user, RedirectAttributes redirectAttributes) {
		log.info("회원가입 요청 - user_id={}", user.getUser_id());
		boolean success = userService.joinUser(user);
		if (!success) {
			log.warn("회원가입 실패 - 이미 존재하는 user_id={}", user.getUser_id());
			redirectAttributes.addFlashAttribute("error", "이미 존재하는 아이디입니다.");
			return "redirect:/user/join";
		}
		redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다. 로그인해주세요.");
		log.info("회원가입 완료 - user_id={}", user.getUser_id());
		return "redirect:/user/login";
	}

	// 로그인 폼
	@GetMapping("/login")
	public String loginForm() {
		return "user/login";
	}

	// 관리자 페이지 등 권한 없는 URL 무단 접근 시 403 에러 안내 화면 연결
	@GetMapping("/denied")
	public String accessDenied(Model model) {
		model.addAttribute("errorCode", "403");
		model.addAttribute("errorTitle", "접근 권한이 없습니다");
		model.addAttribute("errorMessage", "관리자만 접근할 수 있는 페이지이거나 해당 메뉴에 대한 접근 권한이 없습니다.");
		return "error/error";
	}
}
