package com.studyroom.booking.modules.space.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.booking.common.ResultCode;
import com.studyroom.booking.common.exception.BusinessException;
import com.studyroom.booking.modules.space.dto.CampusRequest;
import com.studyroom.booking.modules.space.entity.Building;
import com.studyroom.booking.modules.space.entity.Campus;
import com.studyroom.booking.modules.space.mapper.BuildingMapper;
import com.studyroom.booking.modules.space.mapper.CampusMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 校区 Service
 *
 * @author 陈梦涵
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CampusService extends ServiceImpl<CampusMapper, Campus> {

    private final BuildingMapper buildingMapper;

    /**
     * 获取所有校区列表
     */
    public List<Campus> listAll() {
        LambdaQueryWrapper<Campus> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Campus::getSortOrder);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 根据ID获取校区
     */
    public Campus getById(Long id) {
        Campus campus = baseMapper.selectById(id);
        if (campus == null) {
            throw new BusinessException(ResultCode.CAMPUS_NOT_FOUND);
        }
        return campus;
    }

    /**
     * 新增校区
     */
    @Transactional
    public Campus create(CampusRequest request) {
        Campus campus = new Campus();
        BeanUtil.copyProperties(request, campus);
        if (campus.getSortOrder() == null) {
            campus.setSortOrder(0);
        }
        if (campus.getStatus() == null) {
            campus.setStatus(1);
        }
        baseMapper.insert(campus);
        return campus;
    }

    /**
     * 更新校区
     */
    @Transactional
    public Campus update(Long id, CampusRequest request) {
        Campus campus = getById(id);
        BeanUtil.copyProperties(request, campus);
        campus.setId(id);
        baseMapper.updateById(campus);
        return campus;
    }

    /**
     * 删除校区（逻辑删除）
     * 如果校区下存在楼栋则不允许删除
     */
    @Transactional
    public void delete(Long id) {
        // 确保校区存在
        getById(id);

        // 级联检查：是否存在楼栋
        long buildingCount = buildingMapper.selectCount(
                new LambdaQueryWrapper<Building>()
                        .eq(Building::getCampusId, id)
        );
        if (buildingCount > 0) {
            throw new BusinessException(ResultCode.CAMPUS_HAS_BUILDINGS.getCode(), "校区下存在 " + buildingCount + " 个楼栋，无法删除");
        }

        baseMapper.deleteById(id);
    }
}
