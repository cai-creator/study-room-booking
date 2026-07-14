package com.studyroom.booking.modules.reservation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyroom.booking.modules.reservation.entity.Booking;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预约 Mapper
 *
 * @author 郭学威
 */
@Mapper
public interface BookingMapper extends BaseMapper<Booking> {
}
