# todoProject 완료율 통계 기능

## 1. 개요

- 전체 Todo 중 상태(STATUS)별 개수와 완료율(%)을 계산해 보여주는 기능
- 완료율은 DB에 별도 컬럼으로 저장하지 않고, 조회 시점에 Service 레이어에서 계산
- 상태별 개수 집계는 SQL의 `GROUP BY`로 처리(Service/JS에서 개수를 세지 않음)

## 2. 관련 테이블 (SCOTT.TODO)

```sql
CREATE TABLE "SCOTT"."TODO" (
    "TODO_ID"       NUMBER NOT NULL,
    "TITLE"         NVARCHAR2(50) NOT NULL,
    "CONTENT"       NVARCHAR2(500) NOT NULL,
    "SCHEDULE_DATE" DATE NOT NULL,
    "STATUS"        VARCHAR2(20) DEFAULT 'TODO' NOT NULL,
    "FILE_URL"      NVARCHAR2(255),
    "FILE_NAME"     NVARCHAR2(255),
    "USER_NO"       NUMBER,
    CONSTRAINT "PK_TODO" PRIMARY KEY ("TODO_ID"),
    CONSTRAINT "FK_TODO_USER_TODO" FOREIGN KEY ("USER_NO")
        REFERENCES "SCOTT"."TODO_USER" ("USER_NO")
);
```

- STATUS 값: `TODO`(시작전) / `DOING`(진행중) / `DONE`(완료)
- USER_NO를 기준으로 사용자별 통계를 조회

## 3. 전체 흐름

```
TodoMapper (SQL GROUP BY로 상태별 개수 집계)
       ↓
TodoServiceImpl.getTodoStats() (rows를 순회하며 TodoStatsDTO에 값 채움 + 완료율 계산)
       ↓
TodoController (@GetMapping("/stats") - 로그인 사용자의 user_no로 조회 후 model에 담아 View로 전달)
       ↓
stats.html (Thymeleaf로 값 출력 + Highcharts로 시각화)
```

## 4. Mapper

**TodoMapper.xml**

```xml
<select id="selectStatusCountByUser" parameterType="long" resultType="map">
    SELECT status, COUNT(*) AS CNT
    FROM todo
    WHERE user_no = #{user_no}
    GROUP BY status
</select>
```

**TodoMapper 인터페이스**

```java
List<Map<String, Object>> selectStatusCountByUser(@Param("user_no") Long user_no);
```

- 결과는 `[{STATUS=TODO, CNT=3}, {STATUS=DOING, CNT=1}, {STATUS=DONE, CNT=6}]` 형태
- 특정 상태의 Todo가 0개면 그 상태는 결과에 아예 안 나오므로, Service에서 0으로 초기화 후 덮어쓰는 방식으로 방어

## 5. DTO

```java
package kr.or.oti.project.dto;

import lombok.Data;

@Data
public class TodoStatsDTO {
    private long total_count;
    private long todo_count;
    private long doing_count;
    private long done_count;
    private double completion_rate; // DB 미저장, Service에서 계산
}
```

## 6. Service

**TodoService 인터페이스**

```java
TodoStatsDTO getTodoStats(Long user_no);
```

**TodoServiceImpl**

```java
@Override
public TodoStatsDTO getTodoStats(Long user_no) {

    List<Map<String, Object>> rows = todoMapper.selectStatusCountByUser(user_no);

    TodoStatsDTO stats = new TodoStatsDTO();
    long todo_count = 0;
    long doing_count = 0;
    long done_count = 0;

    for (Map<String, Object> row : rows) {
        String status = (String) row.get("STATUS");
        long cnt = ((Number) row.get("CNT")).longValue();

        switch (status) {
            case "TODO":  todo_count = cnt;  break;
            case "DOING": doing_count = cnt; break;
            case "DONE":  done_count = cnt;  break;
        }
    }

    long total_count = todo_count + doing_count + done_count;

    double completion_rate = (total_count == 0)
            ? 0.0
            : (done_count * 100.0 / total_count);

    stats.setTotal_count(total_count);
    stats.setTodo_count(todo_count);
    stats.setDoing_count(doing_count);
    stats.setDone_count(done_count);
    stats.setCompletion_rate(completion_rate);

    return stats;
}
```

## 7. Controller

```java
@GetMapping("/stats")  // 클래스 @RequestMapping("/todo")와 합쳐져 최종 경로 /todo/stats
public String getTodoStats(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

    Long user_no = userDetails.getUser_no();
    TodoStatsDTO stats = todoService.getTodoStats(user_no);
    model.addAttribute("stats", stats);

    return "todo/stats";
}
```

- 세션에서 직접 꺼내지 않고 Spring Security의 `@AuthenticationPrincipal`로 로그인 사용자 정보를 받음(다른 메서드들과 방식 통일)
- 비로그인 상태로 `/todo/stats` 접근 시, Controller에 도달하기 전에 Spring Security가 로그인 화면으로 리다이렉트

## 8. View (stats.html) + Highcharts

- Highcharts는 CDN 스크립트 태그(`<script src="https://code.highcharts.com/highcharts.js">`) 방식 사용
  - 현재 프로젝트가 Thymeleaf 기반 서버사이드 렌더링 구조라 npm `import Highcharts from "highcharts"` 방식은 부적합(별도 Node.js/번들러 환경 필요)
- `chart.type` 값으로 차트 종류 결정 (`pie`: 비율 강조, `column`: 상태별 개수 비교 등)

```html
<script src="https://code.highcharts.com/highcharts.js"></script>
<div id="statsChart" style="width:400px;height:300px;"></div>

<script th:inline="javascript">
    var todoCount  = /*[[${stats.todo_count}]]*/ 0;
    var doingCount = /*[[${stats.doing_count}]]*/ 0;
    var doneCount  = /*[[${stats.done_count}]]*/ 0;

    Highcharts.chart('statsChart', {
        chart: { type: 'pie' },
        title: { text: '할일 상태 비율' },
        series: [{
            name: '개수',
            data: [
                { name: '시작전', y: todoCount },
                { name: '진행중', y: doingCount },
                { name: '완료', y: doneCount }
            ]
        }]
    });
</script>
```

## 9. 트러블슈팅 기록

- `@RequestMapping("/todo")` + `@GetMapping("/todo/stats")`를 같이 쓰면 최종 경로가 `/todo/todo/stats`가 되어 `/todo/stats` 요청이 `/todo/{todo_id}`로 잘못 매칭되는 문제 발생
  → `@GetMapping("/stats")`로 수정하여 해결
- `session.getAttribute("user_no")` 대신 다른 메서드들과 동일하게 `@AuthenticationPrincipal CustomUserDetails`로 user_no를 획득하도록 통일

## 10. 남은 작업 / 확장 아이디어

- Highcharts 차트 타입(pie / column) 최종 결정 및 스타일링
- 완료율 외 추가 통계(예: 기간별 등록 추이 등) 필요 시 검토