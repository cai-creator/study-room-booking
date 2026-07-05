package com.studyroom.booking.modules.seat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyroom.booking.modules.seat.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预约记录 Mapper（座位管控模块内部使用）
 * <p>
 * 用于座位管控相关操作（签到/签退/暂离/返回）中查询和更新预约状态。
 *
 * @author 邓祺然
 */
@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {
}
