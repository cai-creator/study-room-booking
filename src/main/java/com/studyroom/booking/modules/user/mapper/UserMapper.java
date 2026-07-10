package com.studyroom.booking.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyroom.booking.modules.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User selectByUsernameIncludeDeleted(String username);

    @Update("UPDATE sys_user SET password = #{password}, real_name = #{realName}, email = #{email}, " +
            "phone = #{phone}, role = #{role}, status = #{status}, deleted = #{deleted}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int updateIncludeDeleted(@Param("id") Long id, @Param("password") String password,
                             @Param("realName") String realName, @Param("email") String email,
                             @Param("phone") String phone, @Param("role") String role,
                             @Param("status") Integer status, @Param("deleted") Integer deleted,
                             @Param("updatedAt") LocalDateTime updatedAt);
}