package com.studyroom.booking.modules.space.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyroom.booking.modules.space.entity.SeatUnavailable;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SeatUnavailableMapper extends BaseMapper<SeatUnavailable> {

    List<SeatUnavailable> selectActiveBySeatId(Long seatId);

    List<SeatUnavailable> selectCurrentlyUnavailable(Long seatId, LocalDateTime now);
}