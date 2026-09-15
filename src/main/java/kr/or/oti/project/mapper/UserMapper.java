package kr.or.oti.project.mapper;

import kr.or.oti.project.domain.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    int insertUser(User user);
    User selectUserById(String user_id);
    int countUserById(String user_id);
}