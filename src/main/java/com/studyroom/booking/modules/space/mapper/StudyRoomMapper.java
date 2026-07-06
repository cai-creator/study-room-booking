package com.studyroom.booking.modules.space.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyroom.booking.modules.space.dto.RoomQueryRequest;
import com.studyroom.booking.modules.space.entity.StudyRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 自习室 Mapper
 * <p>
 * 自定义分页查询见 StudyRoomMapper.xml
 *
 * @author 陈梦涵
 */
@Mapper
public interface StudyRoomMapper extends BaseMapper<StudyRoom> {

    /**
     * 分页查询自习室（支持多条件筛选）
     */
    Page<StudyRoom> selectPageWithFilters(Page<StudyRoom> page, @Param("query") RoomQueryRequest query);
}
