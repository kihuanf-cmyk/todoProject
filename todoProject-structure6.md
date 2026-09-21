# todoProject 구조와 요청 흐름

## 한눈에 보기

이 프로젝트는 로그인한 사용자별 Todo 일정 관리 애플리케이션이다. Spring MVC의 Controller, Service, MyBatis Mapper, Oracle TODO 테이블로 구성된다.

    브라우저(Thymeleaf)
      -> Controller: 요청 파라미터 수신·화면 모델 구성
      -> Service: 소유권 검사·트랜잭션·파일 처리
      -> Mapper / MyBatis XML: SQL 실행
      -> Oracle TODO 테이블

첨부파일은 별도 파일 테이블을 사용하지 않는다. Todo 하나당 파일 하나만 연결할 수 있으며, 실제 파일은 서버 디렉터리에, 파일 정보는 TODO 테이블에 저장한다.

## 주요 파일

| 위치 | 역할 |
| --- | --- |
| service/impl/TodoServiceImpl.java | 2단계 등록, 파일 교체·삭제·유지 분기, 소유권 검사 |
| util/FileStorageUtil.java | UUID 파일명 생성, 실제 파일 저장·삭제 |
| domain/Todo.java | TODO 행 객체. file_url, file_name 포함 |
| dto/TodoUpdateRequestDto.java | 수정 요청과 deleteFile 값 |
| mapper/TodoMapper.java | MyBatis 호출 인터페이스 |
| resources/mapper/TodoMapper.xml | Todo 기본·파일 컬럼 SQL |
| templates/todo/modify.html | 지연 파일 삭제와 단일 저장 버튼 |
| controller/FileController.java | 저장 파일을 브라우저에 응답 |

## 페이징 요약

PageRequestDTO는 기본 page=1, amount=10을 사용한다.

    public int getOffset() {
        return (this.page - 1) * this.amount;
    }

TodoMapper.xml은 로그인 사용자 ID와 선택 검색어를 조건으로 적용하고 Oracle OFFSET/FETCH로 해당 페이지의 Todo만 조회한다. PageResponseDTO는 전체 건수로 페이지 번호 블록, 이전·다음 링크를 계산한다.

## 첨부파일 업로드 구조

## 1. 데이터 저장 방식

첨부파일 정보는 다음처럼 나뉜다.

| 위치 | 내용 | 예 |
| --- | --- | --- |
| 서버 파일 시스템 | 실제 파일 바이트 | UPLOADPATH/uuid_원본명.pdf |
| TODO.FILE_URL | 서버 저장 파일명 | uuid_원본명.pdf |
| TODO.FILE_NAME | 사용자가 올린 원본명 | 원본명.pdf |

- FILE_URL은 파일 조회 URL과 물리 파일 삭제에 쓴다.
- FILE_NAME은 화면에 보여 줄 이름이다.
- FILE_URL과 FILE_NAME이 둘 다 NULL이면 첨부파일이 없는 Todo다.
- 파일은 Todo당 최대 하나다. 새 파일 업로드는 기존 파일의 교체다.

FileStorageUtil은 UUID를 원본명 앞에 붙인다.

    savedName = UUID.randomUUID() + "_" + originalName;

따라서 서로 다른 사용자가 같은 이름의 파일을 올려도 서버 저장명이 충돌하지 않는다.

## 2. 폼 요청과 용량 제한

등록·수정 form은 파일 바이너리를 포함해야 하므로 multipart/form-data를 쓴다.

    <form method="post" enctype="multipart/form-data">
      <input type="file" name="file">
    </form>

file이라는 name은 TodoController의 MultipartFile file 파라미터와 연결된다. 설정상 파일 하나는 최대 10 MB, 요청 전체는 최대 30 MB다.

    spring.servlet.multipart.max-file-size=10MB
    spring.servlet.multipart.max-request-size=30MB
    file.upload-dir=UPLOADPATH 환경변수

UPLOADPATH는 실행 환경에서 지정해야 할 파일 저장 디렉터리다. 수정 화면은 파일 선택 시 파일명과 이미지 미리보기를 제공한다. 화면의 accept="image/*"는 선택 창을 돕는 UI 힌트이며, 서버 측 확장자 제한은 현재 구현되어 있지 않다.

용량을 초과하면 MaxUploadSizeExceededException이 발생하고 GlobalExceptionHandler가 400 오류 화면으로 처리한다.

## 3. 등록: 2단계 저장

등록은 null 파일을 안전하게 처리하기 위해 두 단계로 실행된다. saveTodo에는 Transactional이 적용된다.

    1. TODO 기본 정보 INSERT
    2. 파일이 존재할 때만 디스크에 파일 저장
    3. TODO의 FILE_URL, FILE_NAME UPDATE

insertTodo SQL은 파일 컬럼을 넣지 않는다.

    INSERT INTO todo
      (todo_id, user_id, title, content, schedule_date, status)
    VALUES
      (#{todo_id}, #{user_id}, #{title}, #{content}, #{schedule_date}, #{status})

MyBatis selectKey가 Oracle todo_seq.NEXTVAL로 todo_id를 먼저 채운다. 기본 Todo가 저장된 후, hasFile(file)가 true일 때만 다음 처리가 진행된다.

    todoMapper.insertTodo(todo);

    if (hasFile(file)) {
        saveAndUpdateFile(todo, file, userId);
    }

saveAndUpdateFile은 실제 파일을 저장하고 Todo 객체에 file_url과 file_name을 설정한 뒤 updateTodoFile을 호출한다.

    UPDATE todo
    SET file_url = #{file_url},
        file_name = #{file_name}
    WHERE todo_id = #{todo_id}

파일이 없으면 2~3단계가 실행되지 않으므로 null 파일로 인한 파일 저장 예외가 없다.

## 4. 수정 화면: 지연 삭제

수정 화면에는 독립된 첨부파일 삭제 form이 없다. 파일 삭제 버튼은 type="button"이므로 즉시 서버 요청을 보내지 않는다.

    <input type="hidden" name="deleteFile" id="deleteFile" value="false">
    <button type="button" onclick="markFileForDeletion()">첨부파일 삭제</button>

클릭 시 JavaScript는 삭제 의도를 hidden input에 기록하고 현재 파일 영역만 숨긴다.

    function markFileForDeletion() {
      document.getElementById('deleteFile').value = 'true';
      document.getElementById('currentFileSection').style.display = 'none';
    }

그 뒤 사용자가 최종 저장 버튼을 눌러야 POST /todo/update가 전송된다. boolean deleteFile은 TodoUpdateRequestDto의 deleteFile 필드에 바인딩된다. 즉 제목·내용·상태 수정과 첨부파일 삭제가 하나의 요청, 하나의 DB 트랜잭션으로 처리된다.

## 5. 수정: 세 가지 파일 처리 분기

updateTodo도 Transactional이며 먼저 getOwnedTodo로 Todo 존재 여부와 현재 로그인 사용자의 소유권을 확인한다. 기본 Todo 정보 업데이트 뒤에는 다음 우선순위로 파일을 처리한다.

| 조건 | DB 처리 | 물리 파일 처리 |
| --- | --- | --- |
| A. 새 file이 있음 | updateTodoFile로 FILE_URL/FILE_NAME 교체 | 새 파일 저장 후 이전 파일 삭제 |
| B. 새 file 없음 + deleteFile=true + 기존 파일 있음 | deleteTodoFile로 두 컬럼을 NULL 처리 | 기존 파일 삭제 |
| C. 새 file 없음 + deleteFile=false | 파일 SQL 없음 | 기존 파일 유지 |

Case A가 Case B보다 우선한다. 따라서 사용자가 기존 파일 삭제를 눌렀더라도 새 파일을 선택해 저장하면 최종 결과는 삭제가 아니라 새 파일 교체다.

Case A의 안전한 실행 순서는 다음과 같다.

    새 파일 디스크 저장
    -> TODO 파일 컬럼 UPDATE
    -> 이전 파일 디스크 삭제

이 순서는 새 파일 저장 또는 DB 갱신이 실패했을 때 기존 파일을 잃지 않기 위한 것이다. 새 파일의 DB 반영이 실패하면 saveAndUpdateFile은 방금 저장한 새 물리 파일을 deleteFile로 정리하고 예외를 다시 던진다. DB 트랜잭션은 롤백되고 참조되지 않는 새 파일도 남지 않는다.

Case B의 파일 컬럼 처리 SQL은 다음과 같다.

    UPDATE todo
    SET file_url = NULL,
        file_name = NULL
    WHERE todo_id = #{todo_id}

SQL이 정상 반영된 후 기존 물리 파일을 삭제한다. Case C는 파일 관련 Mapper나 물리 파일 작업을 전혀 수행하지 않는다.

## 6. 파일 조회와 표시

상세와 수정 화면은 todo.file_url이 있을 때만 첨부 영역을 보인다.

- jpg, jpeg, png, gif는 /files/{file_url}을 img src로 사용해 이미지 미리보기를 표시한다.
- 그 외에는 같은 URL을 파일 링크로 제공한다.
- FileController는 UPLOADPATH에서 해당 파일을 FileSystemResource로 읽어 inline 응답한다.
- 요청 파일이 없으면 FileController는 404 응답과 warn 로그를 남긴다.

## 7. Todo 삭제와 고아 파일 방지

Todo 자체를 삭제할 때는 소유권을 확인하고 TODO 행을 삭제한 뒤 FILE_URL의 물리 파일도 삭제한다. 파일 교체·파일 삭제·Todo 삭제 경로 모두 FileStorageUtil.deleteFile을 호출하므로 서버 디렉터리에 참조되지 않는 파일이 쌓이는 것을 줄인다.

DB 트랜잭션은 Oracle 변경만 롤백하며 파일 시스템 변경까지 자동 복구하지는 않는다. 그래서 새 파일 저장 후 DB 갱신 실패라는 대표적인 고아 파일 상황에는 Service가 직접 파일 삭제 보상 처리를 한다.

## 8. 로그와 예외 처리

| 상황 | 로그 | 예외/결과 |
| --- | --- | --- |
| 파일 저장·교체·삭제 성공 | info 또는 debug | 정상 진행 |
| 없는 Todo 또는 삭제할 파일 없음 | warn | IllegalArgumentException, 404 화면 |
| 다른 사용자의 Todo 접근 | warn | AccessDeniedException, 403 화면 |
| 파일 컬럼 UPDATE/DELETE 실패 | error | IllegalStateException, 500 화면 및 DB 롤백 |
| 업로드 경로 생성 또는 파일 저장 실패 | error | RuntimeException 또는 IllegalStateException, 500 화면 |
| 파일 용량 초과 | warn | MaxUploadSizeExceededException, 400 화면 |

## 관련 코드 빠른 참조

- src/main/java/kr/or/oti/project/service/impl/TodoServiceImpl.java
- src/main/java/kr/or/oti/project/util/FileStorageUtil.java
- src/main/java/kr/or/oti/project/dto/TodoUpdateRequestDto.java
- src/main/resources/mapper/TodoMapper.xml
- src/main/resources/templates/todo/modify.html
- src/main/resources/templates/todo/read.html
- src/main/java/kr/or/oti/project/controller/FileController.java
- src/main/java/kr/or/oti/project/controller/GlobalExceptionHandler.java
