package com.studyroom.booking.modules.space.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.booking.common.ResultCode;
import com.studyroom.booking.common.exception.BusinessException;
import com.studyroom.booking.modules.space.dto.FloorRequest;
import com.studyroom.booking.modules.space.entity.Building;
import com.studyroom.booking.modules.space.entity.Floor;
import com.studyroom.booking.modules.space.entity.StudyRoom;
import com.studyroom.booking.modules.space.mapper.BuildingMapper;
import com.studyroom.booking.modules.space.mapper.FloorMapper;
import com.studyroom.booking.modules.space.mapper.StudyRoomMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 楼层 Service
 *
 * @author 陈梦涵
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FloorService extends ServiceImpl<FloorMapper, Floor> {

    private final BuildingMapper buildingMapper;
    private final StudyRoomMapper studyRoomMapper;

    /**
     * 获取楼层列表（必须指定楼栋ID）
     */
    public List<Floor> listByBuildingId(Long buildingId) {
        LambdaQueryWrapper<Floor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Floor::getBuildingId, buildingId);
        wrapper.orderByAsc(Floor::getFloorNumber);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 根据ID获取楼层
     */
    public Floor getById(Long id) {
        Floor floor = baseMapper.selectById(id);
        if (floor == null) {
            throw new BusinessException(ResultCode.FLOOR_NOT_FOUND);
        }
        return floor;
    }

    /**
     * 新增楼层
     */
    @Transactional
    public Floor create(FloorRequest request) {
        // 校验楼栋是否存在
        Building building = buildingMapper.selectById(request.getBuildingId());
        if (building == null) {
            throw new BusinessException(ResultCode.BUILDING_NOT_FOUND);
        }

        Floor floor = new Floor();
        BeanUtil.copyProperties(request, floor);
        if (floor.getSortOrder() == null) {
            floor.setSortOrder(request.getFloorNumber());
        }
        if (floor.getStatus() == null) {
            floor.setStatus(1);
        }
        // 默认名称
        if (floor.getName() == null || floor.getName().isBlank()) {
            floor.setName(request.getFloorNumber() + "楼");
        }

        baseMapper.insert(floor);

        // 同步楼栋的楼层数
        syncBuildingFloorCount(request.getBuildingId());

        return floor;
    }

    /**
     * 更新楼层
     */
    @Transactional
    public Floor update(Long id, FloorRequest request) {
        Floor floor = getById(id);
        Long oldBuildingId = floor.getBuildingId();

        // 如果修改了所属楼栋，校验新楼栋是否存在
        if (request.getBuildingId() != null && !request.getBuildingId().equals(oldBuildingId)) {
            if (buildingMapper.selectById(request.getBuildingId()) == null) {
                throw new BusinessException(ResultCode.BUILDING_NOT_FOUND);
            }
        }

        BeanUtil.copyProperties(request, floor);
        floor.setId(id);
        baseMapper.updateById(floor);

        // 如果楼栋发生变化，同步两个楼栋的楼层数
        if (request.getBuildingId() != null && !request.getBuildingId().equals(oldBuildingId)) {
            syncBuildingFloorCount(oldBuildingId);
            syncBuildingFloorCount(request.getBuildingId());
        }

        return floor;
    }

    /**
     * 删除楼层（逻辑删除）
     * 如果楼层下存在自习室则不允许删除
     */
    @Transactional
    public void delete(Long id) {
        Floor floor = getById(id);

        long roomCount = studyRoomMapper.selectCount(
                new LambdaQueryWrapper<StudyRoom>()
                        .eq(StudyRoom::getFloorId, id)
        );
        if (roomCount > 0) {
            throw new BusinessException(ResultCode.FLOOR_HAS_ROOMS.getCode(), "楼层下存在 " + roomCount + " 个自习室，无法删除");
        }

        baseMapper.deleteById(id);

        // 同步楼栋的楼层数
        syncBuildingFloorCount(floor.getBuildingId());
    }

    /**
     * 同步楼栋的楼层数
     */
    private void syncBuildingFloorCount(Long buildingId) {
        long count = baseMapper.selectCount(
                new LambdaQueryWrapper<Floor>()
                        .eq(Floor::getBuildingId, buildingId)
        );
        Building building = new Building();
        building.setId(buildingId);
        building.setFloorCount((int) count);
        buildingMapper.updateById(building);
    }
}
