package com.studyroom.booking.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyroom.booking.modules.user.entity.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface RefreshTokenMapper extends BaseMapper<RefreshToken> {

    int deleteByUserId(@Param("userId") Long userId);

    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    int markTokenUsed(@Param("token") String token);

    RefreshToken selectByToken(@Param("token") String token);

    List<RefreshToken> selectByUserId(@Param("userId") Long userId);
}