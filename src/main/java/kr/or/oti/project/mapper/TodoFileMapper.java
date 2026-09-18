package kr.or.oti.project.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import kr.or.oti.project.domain.TodoFile;

@Mapper
public interface TodoFileMapper {

	// 파일 1건 등록 (여러 개면 Service에서 반복 호출)
	int insertTodoFile(TodoFile todoFile);

	// 특정 todo_id에 달린 첨부파일 전체 조회
	List<TodoFile> selectFilesByTodoId(Long todo_id);

	// 특정 todo_id의 첨부파일 전체 삭제 (수정 시 재업로드하거나, 게시글 삭제 시 사용)
	int deleteFilesByTodoId(Long todo_id);

	// 첨부파일 1건만 삭제 (수정 화면에서 개별 삭제 버튼 누를 때)
	int deleteTodoFileById(Long file_id);

	// 삭제 전 소유권 검증을 위해 파일 1건 조회 (해당 파일이 어느 todo_id에 속하는지 확인)
	TodoFile selectFileById(Long file_id);
}