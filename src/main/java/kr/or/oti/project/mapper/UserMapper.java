package kr.or.oti.project.mapper;

import org.apache.ibatis.annotations.Mapper;

import kr.or.oti.project.domain.User;

@Mapper
public interface UserMapper {
    int insertUser(User user);
    User selectUserById(String user_id);
    User selectUserByNo(Long user_no);
    int countUserById(String user_id);
}