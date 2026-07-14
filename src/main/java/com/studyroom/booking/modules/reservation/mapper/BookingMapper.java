package com.studyroom.booking.modules.reservation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyroom.booking.modules.reservation.entity.Booking;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预约 Mapper
 *
 * @author 郭学威
 */
@Mapper
public interface BookingMapper extends BaseMapper<Booking> {

    /**
     * 物理删除预约记录（绕过 @TableLogic）
     * 用于释放唯一约束 (seat_id, start_time, end_time)
     *
     * @param id 预约ID
     * @return 影响行数
     */
    @Delete("DELETE FROM reservation WHERE id = #{id}")
    int physicalDeleteById(Long id);
}
