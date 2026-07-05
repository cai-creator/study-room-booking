package com.studyroom.booking.modules.seat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyroom.booking.modules.seat.entity.NoShowRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 爽约记录 Mapper
 *
 * @author 邓祺然
 */
@Mapper
public interface NoShowRecordMapper extends BaseMapper<NoShowRecord> {
}
