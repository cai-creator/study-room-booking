package com.studyroom.booking.modules.seat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyroom.booking.modules.seat.entity.SeatControl;
import org.apache.ibatis.annotations.Mapper;

/**
 * 座位 Mapper（座位管控模块内部使用）
 *
 * @author 邓祺然
 */
@Mapper
public interface SeatControlMapper extends BaseMapper<SeatControl> {
}
