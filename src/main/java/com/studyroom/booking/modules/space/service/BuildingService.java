package com.studyroom.booking.modules.space.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.booking.common.exception.BusinessException;
import com.studyroom.booking.modules.space.dto.BuildingRequest;
import com.studyroom.booking.modules.space.entity.Building;
import com.studyroom.booking.modules.space.entity.Floor;
import com.studyroom.booking.modules.space.mapper.BuildingMapper;
import com.studyroom.booking.modules.space.mapper.CampusMapper;
import com.studyroom.booking.modules.space.mapper.FloorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.hutool.core.bean.BeanUtil;

import java.util.List;

/**
 * 楼栋 Service
 *
 * @author 陈梦涵
 */
@Service
@RequiredArgsConstructor
public class BuildingService extends ServiceImpl<BuildingMapper, Building> {

    private final CampusMapper campusMapper;
    private final FloorMapper floorMapper;

    /**
     * 获取楼栋列表（支持按校区筛选）
     */
    public List<Building> listByCampusId(Long campusId) {
        LambdaQueryWrapper<Building> wrapper = new LambdaQueryWrapper<>();
        if (campusId != null) {
            wrapper.eq(Building::getCampusId, campusId);
        }
        wrapper.orderByAsc(Building::getSortOrder);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 根据ID获取楼栋
     */
    public Building getById(Long id) {
        Building building = baseMapper.selectById(id);
        if (building == null) {
            throw new BusinessException(404, "楼栋不存在");
        }
        return building;
    }

    /**
     * 新增楼栋
     */
    @Transactional
    public Building create(BuildingRequest request) {
        // 校验校区是否存在
        if (campusMapper.selectById(request.getCampusId()) == null) {
            throw new BusinessException(404, "所属校区不存在");
        }

        Building building = new Building();
        BeanUtil.copyProperties(request, building);
        if (building.getFloorCount() == null) {
            building.setFloorCount(0);
        }
        if (building.getSortOrder() == null) {
            building.setSortOrder(0);
        }
        if (building.getStatus() == null) {
            building.setStatus(1);
        }
        baseMapper.insert(building);
        return building;
    }

    /**
     * 更新楼栋
     */
    @Transactional
    public Building update(Long id, BuildingRequest request) {
        Building building = getById(id);

        // 如果修改了校区，校验新校区是否存在
        if (request.getCampusId() != null && !request.getCampusId().equals(building.getCampusId())) {
            if (campusMapper.selectById(request.getCampusId()) == null) {
                throw new BusinessException(404, "所属校区不存在");
            }
        }

        BeanUtil.copyProperties(request, building);
        building.setId(id);
        // floorCount 由楼层变更时自动同步，不通过更新接口修改
        building.setFloorCount(null);
        baseMapper.updateById(building);
        return building;
    }

    /**
     * 删除楼栋（逻辑删除）
     * 如果楼栋下存在楼层则不允许删除
     */
    @Transactional
    public void delete(Long id) {
        getById(id);

        long floorCount = floorMapper.selectCount(
                new LambdaQueryWrapper<Floor>()
                        .eq(Floor::getBuildingId, id)
        );
        if (floorCount > 0) {
            throw new BusinessException(2102, "楼栋下存在 " + floorCount + " 个楼层，无法删除");
        }

        baseMapper.deleteById(id);
    }
}
