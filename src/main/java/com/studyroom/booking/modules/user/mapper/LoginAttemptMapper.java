package com.studyroom.booking.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyroom.booking.modules.user.entity.LoginAttempt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoginAttemptMapper extends BaseMapper<LoginAttempt> {

    LoginAttempt selectByUserId(@Param("userId") Long userId);

    int resetFailCount(@Param("userId") Long userId);
}
