package com.studyroom.booking.modules.space.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyroom.booking.modules.space.entity.Seat;
import org.apache.ibatis.annotations.Mapper;

/**
 * 座位 Mapper
 *
 * @author 陈梦涵
 */
@Mapper
public interface SeatMapper extends BaseMapper<Seat> {
}
