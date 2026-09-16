##면접질문 1

이 프로젝트는 스프링 시큐리티, 인증/인가, 보안 예외 처리, 서버 유효성 검사까지 1달 차 신입이 갖추어야 할 핵심이 알차게 들어있어서 면접관들이 물어볼 거리가 아주 많습니다!

면접관이 자주 던지는 핵심 질문 8가지와 합격을 부르는 답변 팁을 정리해 드릴게요.

1. Spring Security & 인증/인가 관련 (가장 중요! ⭐⭐⭐)
❓ Q1. 로그인 기능을 구현할 때 Spring Security를 도입한 이유가 무엇인가요?
면접관의 의도: 시큐리티를 그냥 따라 쳤는지, 필요성을 알고 썼는지 확인
💡 답변 팁:
"단순 세션 방식은 URL마다 로그인 여부를 직접 체크해야 해서 코드 중복이 심합니다. Spring Security를 사용하면 필터 체인 단계에서 URL 접근 권한을 한곳에서 중앙 제어할 수 있고, 비밀번호 암호화(BCrypt)와 세션 관리까지 안전하고 표준화된 방식으로 구현할 수 있어서 도입했습니다."

❓ Q2. 비밀번호를 저장할 때 왜 BCryptPasswordEncoder를 사용했나요?
면접관의 의도: 암호화 방식에 대한 기본 개념 보유 여부
💡 답변 팁:
"비밀번호는 복호화(원래대로 되돌리기)가 불가능한 단방향 해시 암호화로 저장해야 안전합니다. BCrypt는 무작위 소금값(Salt)을 섞어 동일한 비밀번호라도 매번 다른 해시값을 생성하므로, 해커가 비밀번호를 유추하기 매우 까다로워 채택했습니다."

❓ Q3. 다른 사람의 일정을 몰래 조회하거나 삭제하는 건 어떻게 막았나요? (소유권 검증)
면접관의 의도: 보안 취약점(IDOR/BOLA)에 대한 인지 및 처리 경험
💡 답변 팁:
"단순히 일정 번호(todo_id)로만 조회나 삭제를 하면 다른 사람의 번호를 입력해 접근할 수 있는 위험이 있었습니다. 그래서 Service 레이어에서 '조회한 일정의 작성자 아이디'와 '현재 SecurityContext에서 꺼낸 로그인 아이디'가 일치하는지 소유권 검증을 거치도록 짰고, 일치하지 않으면 AccessDeniedException(403)을 던져 차단했습니다."

2. 예외 처리 & 입력값 검증 관련 ⭐⭐
❓ Q4. @ControllerAdvice를 사용해서 예외 처리를 하셨는데, 왜 이렇게 하셨나요?
면접관의 의도: 공통 예외 처리 체계 이해도
💡 답변 팁:
"에러가 날 때마다 컨트롤러마다 try-catch를 작성하면 코드가 지저분해집니다. @ControllerAdvice를 사용하면 모든 컨트롤러의 에러를 한곳에서 모아 처리할 수 있습니다. 400(입력값 오류), 403(권한 없음), 404(없음), 500(서버 오류)별로 에러 코드와 친절한 메시지를 담아 예쁜 에러 화면으로 안내하도록 구현했습니다."

❓ Q5. HTML에 required 속성이 있는데, 왜 서버에서 @Valid(Bean Validation)를 또 검사해야 하나요?
면접관의 의도: 프론트엔드 검증 vs 백엔드 검증의 차이 이해
💡 답변 팁:
"클라이언트(HTML/JS) 검증은 개발자 도구(F12)나 Postman 같은 도구로 사용자가 얼마든지 우회할 수 있습니다. 시스템 안정성과 DB 데이터 무결성을 지키려면 반드시 백엔드 서버 단에서 @Valid와 @NotBlank, @Size로 2차 검증을 거쳐야 빈 값이나 글자 수 초과 데이터를 안전하게 막을 수 있습니다."

3. MyBatis & DB 관련 ⭐⭐
❓ Q6. MyBatis에서 SQL을 쓸 때 #{}와 ${}의 차이가 무엇인가요?
면접관의 의도: SQL 인젝션 해킹 방어 개념을 아는지 질문 (단골 질문!)
💡 답변 팁:
"#{}는 PreparedStatement 방식으로 값을 물리적인 문자열 데이터로 취급하여 SQL 인젝션 공격을 자동으로 방어해 줍니다. 반면 ${}는 문자열을 그대로 SQL에 이어 붙이기 때문에 해킹 위험이 있습니다. 그래서 저희 프로젝트에서는 안전한 #{} 방식만 사용했습니다."

❓ Q7. Mapper 인터페이스에서 @Param 어노테이션은 왜 붙이셨나요?
면접관의 의도: MyBatis 파라미터 매핑 원리 이해
💡 답변 팁:
"MyBatis에서 메서드 파라미터가 2개 이상일 때(user_id, keyword), XML 매퍼에서 어느 이름이 어느 변수인지 혼동할 수 있습니다. @Param("이름")을 붙여주면 XML의 #{user_id}, #{keyword}와 정확히 1:1로 매핑되도록 보장해 줍니다."

4. 꼬리 질문 (프로젝트 개선점 관련) 💡
❓ Q8. 현재 일정 목록 조회가 페이징이 안 되어 있는데, 데이터가 10만 건이 되면 어떻게 하실 건가요?
면접관의 의도: 성능 최적화에 대한 고민 여부
💡 답변 팁:
"현재는 전체 목록을 조회하고 있지만, 데이터가 많아지면 응답 속도가 느려질 수 있습니다. 향후 개선 시 Oracle의 OFFSET / FETCH NEXT 문법이나 ROWNUM을 활용해 **한 페이지당 10개씩 끊어 가져오는 페이징(Pageable)**을 적용할 계획입니다. 또한 WHERE user_id = ? ORDER BY schedule_date 쿼리 성능을 위해 (user_id, schedule_date) 복합 인덱스를 걸어 성능을 최적화할 생각입니다."

🎯 면접 꿀팁 요약

이 대답들을 다 외우실 필요는 전혀 없습니다! 핵심은 **"내가 단순히 코드를 복사해서 쓴 게 아니라, 왜 이 기술을 썼고(Why), 에러나 보안 문제를 어떻게 해결했는지(How)를 알고 있다"**는 태도를 보여주시는 것입니다. 이 정도 답변만 하셔도 1달 차 신입 기준 **상위 10%**입니다!

##면접질문2
1. 보안 설정 코드 (CustomSecurityConfig.java)
❓ Q1. getAuthorities()에서 왜 역할(Role) 앞에 "ROLE_"을 붙이셨나요?
java


// CustomUserDetails.java
return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
💡 답변: "스프링 시큐리티의 hasRole('ADMIN') 설정은 내부적으로 권한 문자열 앞에 ROLE_이라는 접두사(Prefix)가 붙어있다고 가정하고 검사합니다. 그래서 DB에 ADMIN이라고 저장되어 있어도 시큐리티 신분증에 넣을 때는 "ROLE_ADMIN"으로 변환해 주어야 권한 체크가 정상 작동하기 때문입니다."
❓ Q2. 정적 자원(CSS, JS)은 왜 보안 필터에서 제외(ignoring)했나요?
java


// CustomSecurityConfig.java
web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
💡 답변: "CSS, 자바스크립트, 이미지 같은 정적 파일은 로그인 여부와 관계없이 누구나 불러올 수 있어야 합니다. 이 자원들까지 보안 필터 체인을 통과하게 만들면 불필요한 서버 연산과 성능 낭비가 발생하므로 ignoring()으로 보안 검사를 건너뛰도록 설정했습니다."
2. 컨트롤러 & 예외 처리 코드 (TodoController.java, GlobalExceptionHandler.java)
❓ Q3. @RequestParam(required = false)에서 required = false는 무슨 뜻인가요?
java


// TodoController.java
public String list(..., @RequestParam(required = false) String keyword)
💡 답변: "기본값(required = true)으로 두면 주소에 keyword 파라미터가 없을 때 400 에러가 터집니다. required = false로 지정해야 사용자가 검색어를 입력하지 않고 단순 목록 페이지(/todo/list)에 들어왔을 때도 에러 없이 keyword에 null이 들어가 정상 동작하기 때문입니다."
❓ Q4. 예외 처리 코드에서 이 길고 복잡한 코드는 무엇을 가져오는 건가요?
java


// GlobalExceptionHandler.java
String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
💡 답변: "@Valid 검증이 실패했을 때 에러 목록이 e.getBindingResult()에 담깁니다. 그중 첫 번째 에러(getAllErrors().get(0))를 꺼내서, 우리가 DTO에 적어둔 @NotBlank(message = "일정 제목은 필수 입력 항목입니다.")의 메시지 문자열(getDefaultMessage())을 추출해 화면에 보여주는 코드입니다."
3. 서비스 & DTO 코드 (TodoServiceImpl.java, TodoResponseDto.java)
❓ Q5. DB 엔티티(Todo)를 그대로 화면으로 안 보내고, 왜 DTO로 변환해서 보내나요?
java


// TodoServiceImpl.java
return todoMapper.selectTodoListByUser(user_id).stream()
        .map(TodoResponseDto::from)
        .collect(Collectors.toList());
💡 답변: "DB 엔티티(Todo)는 DB 테이블 구조와 직결되어 있어서, 화면으로 그대로 내보내면 보안상 민감한 정보가 노출되거나 화면 요구사항이 바뀔 때 DB 구조까지 영향을 받는 문제가 생깁니다. 그래서 화면에 필요한 데이터만 딱 골라 담은 TodoResponseDto로 변환해서 전달하는 것이 계층 간 분리(Decoupling) 측면에서 안전합니다."
❓ Q6. TodoResponseDto.from() 정적 팩토리 메서드와 @Builder를 쓴 이유는?
java


// TodoResponseDto.java
public static TodoResponseDto from(Todo todo) {
    return TodoResponseDto.builder().title(todo.getTitle()).build();
}
💡 답변: "new TodoResponseDto(...)처럼 생성자를 직접 쓰면 필드 순서가 헷갈려 실수하기 쉽습니다. @Builder를 쓰면 필드 이름을 명시하면서 가독성 높게 객체를 생성할 수 있고, from() 같은 정적 팩토리 메서드를 만들어두면 엔티티 ➔ DTO 변환 로직을 한 곳에서 모아 관리할 수 있어 서비스 코드가 훨씬 깨끗해집니다."
❓ Q7. updateTodo나 deleteTodo에서 int updatedRows 반환값을 왜 검사하나요?
java


// TodoServiceImpl.java
int updatedRows = todoMapper.updateTodo(todo);
if (updatedRows == 0) {
    throw new IllegalArgumentException("수정할 일정을 찾을 수 없습니다.");
}
💡 답변: "SQL의 UPDATE나 DELETE 문은 존재하지 않는 ID로 실행해도 에러가 나지 않고 단순히 0건 수정/삭제되고 성공 처리됩니다. 그래서 실제로 DB 영향(수정/삭제)을 받은 행의 개수가 0인지 확인하여, 대상이 없을 때는 의도적으로 예외를 던져 사용자에게 알리기 위해 검사했습니다."
4. MyBatis SQL 코드 (TodoMapper.xml)
❓ Q8. <selectKey order="BEFORE">는 왜 사용하셨나요?
xml


<!-- TodoMapper.xml -->
<selectKey keyProperty="todo_id" resultType="Long" order="BEFORE">
    SELECT todo_seq.NEXTVAL FROM DUAL
</selectKey>
💡 답변: "Oracle은 MySQL과 달리 자동 증가(AUTO_INCREMENT)가 없고 시퀀스(todo_seq)를 씁니다. INSERT 실행 전에(order="BEFORE") 시퀀스 번호를 먼저 채번해서 todo 객체의 todo_id에 미리 넣어두어야, INSERT 문이 실행될 때 생성된 PK 번호를 자바 코드에서도 즉시 활용할 수 있기 때문입니다."
❓ Q9. Oracle SQL에서 LIKE '%' || #{keyword} || '%' 처럼 ||를 쓴 이유는?
xml


<!-- TodoMapper.xml -->
WHERE title LIKE '%' || #{keyword} || '%'
💡 답변: "Oracle SQL에서 ||는 문자열을 이어 붙이는(Concatenate) 연산자입니다. 안전한 #{keyword} 파라미터 앞뒤로 % 기호를 이어 붙여서, 제목에 검색어가 들어간 데이터를 찾는 LIKE '%검색어%' 문장을 만들기 위해 썼습니다."
5. 사용자 정보 조회 코드 (CustomUserDetailsService.java)
❓ Q10. 회원이 없을 때 왜 하필 UsernameNotFoundException을 던져야 하나요?
java


// CustomUserDetailsService.java
if (user == null) {
    throw new UsernameNotFoundException("존재하지 않는 아이디입니다: " + user_id);
}
💡 답변: "UserDetailsService 인터페이스의 규약(Contract) 때문입니다. 스프링 시큐리티의 AuthenticationManager는 이 메서드에서 UsernameNotFoundException이 던져지는 것을 감지해야 '아, 회원이 없어서 로그인 실패했구나'라고 인지하고 정상적인 로그인 실패 로직으로 넘어가기 때문입니다."
💡 면접 팁 요약

면접관이 특정 코드를 집어서 질문할 때는 **"이 코드가 왜 거기에 붙어있는지(이유)"**와 **"안 붙였을 때 무슨 문제가 생기는지(효과)"**를 아는지가 핵심입니다! 위의 핵심 포인트 한 문장씩만 떠올리시면 답변하기 아주 수월하실 겁니다.