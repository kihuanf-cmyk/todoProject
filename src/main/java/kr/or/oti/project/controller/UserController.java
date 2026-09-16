package kr.or.oti.project.controller;

import javax.servlet.http.HttpSession;
import kr.or.oti.project.domain.User;
import kr.or.oti.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
		boolean success = userService.joinUser(user);
		if (!success) {
			redirectAttributes.addFlashAttribute("error", "이미 존재하는 아이디입니다.");
			return "redirect:/user/join";
		}
		redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다. 로그인해주세요.");
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