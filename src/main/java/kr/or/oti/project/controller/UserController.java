package kr.or.oti.project.controller;

import javax.servlet.http.HttpSession;
import kr.or.oti.project.domain.User;
import kr.or.oti.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

//수정 (로그인/로그아웃 POST·GET 제거, 회원가입 관련만 남김)
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

	// 로그인 폼 (렌더링만 담당 — POST 처리는 Spring Security formLogin이 가로챔)
	@GetMapping("/login")
	public String loginForm() {
		return "user/login";
	}
}