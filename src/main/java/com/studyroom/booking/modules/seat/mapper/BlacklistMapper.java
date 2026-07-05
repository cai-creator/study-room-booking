package com.studyroom.booking.modules.seat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyroom.booking.modules.seat.entity.Blacklist;
import org.apache.ibatis.annotations.Mapper;

/**
 * 黑名单 Mapper
 *
 * @author 邓祺然
 */
@Mapper
public interface BlacklistMapper extends BaseMapper<Blacklist> {
}
