package com.studyroom.booking.modules.space.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.booking.common.exception.BusinessException;
import com.studyroom.booking.modules.space.dto.RoomRequest;
import com.studyroom.booking.modules.space.dto.RoomQueryRequest;
import com.studyroom.booking.modules.space.entity.*;
import com.studyroom.booking.modules.space.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

/**
 * 自习室 Service
 *
 * @author 陈梦涵
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudyRoomService extends ServiceImpl<StudyRoomMapper, StudyRoom> {

    private final CampusMapper campusMapper;
    private final BuildingMapper buildingMapper;
    private final FloorMapper floorMapper;
    private final SeatMapper seatMapper;

    /**
     * 分页查询自习室列表（支持多条件筛选）
     */
    public Page<StudyRoom> listWithFilters(RoomQueryRequest query) {
        // 限制最大分页大小
        if (query.getPageSize() > 100) {
            query.setPageSize(100);
        }
        if (query.getPageNum() == null || query.getPageNum() < 1) {
            query.setPageNum(1);
        }

        Page<StudyRoom> page = new Page<>(query.getPageNum(), query.getPageSize());
        return baseMapper.selectPageWithFilters(page, query);
    }

    /**
     * 根据ID获取自习室详情
     */
    public StudyRoom getById(Long id) {
        StudyRoom room = baseMapper.selectById(id);
        if (room == null) {
            throw new BusinessException(2001, "自习室不存在");
        }
        return room;
    }

    /**
     * 新增自习室
     */
    @Transactional
    public StudyRoom create(RoomRequest request) {
        // 校验楼层是否存在
        Floor floor = floorMapper.selectById(request.getFloorId());
        if (floor == null) {
            throw new BusinessException(404, "所属楼层不存在");
        }

        StudyRoom room = new StudyRoom();
        BeanUtil.copyProperties(request, room);
        if (room.getTotalSeats() == null) {
            room.setTotalSeats(0);
        }
        if (room.getRowsCount() == null) {
            room.setRowsCount(0);
        }
        if (room.getColsCount() == null) {
            room.setColsCount(0);
        }
        if (room.getOpenTime() == null) {
            room.setOpenTime(LocalTime.of(8, 0));
        }
        if (room.getCloseTime() == null) {
            room.setCloseTime(LocalTime.of(22, 0));
        }
        if (room.getRoomType() == null) {
            room.setRoomType("LIBRARY");
        }
        if (room.getStatus() == null) {
            room.setStatus(1);
        }

        baseMapper.insert(room);
        return room;
    }

    /**
     * 更新自习室
     */
    @Transactional
    public StudyRoom update(Long id, RoomRequest request) {
        StudyRoom room = getById(id);

        // 如果修改了楼层，校验新楼层是否存在
        if (request.getFloorId() != null && !request.getFloorId().equals(room.getFloorId())) {
            if (floorMapper.selectById(request.getFloorId()) == null) {
                throw new BusinessException(404, "所属楼层不存在");
            }
        }

        BeanUtil.copyProperties(request, room);
        room.setId(id);
        // 座位数相关字段由座位管理维护，不通过更新接口修改
        room.setTotalSeats(null);
        room.setRowsCount(null);
        room.setColsCount(null);

        baseMapper.updateById(room);
        return room;
    }

    /**
     * 修改自习室状态
     */
    @Transactional
    public void updateStatus(Long id, Integer status) {
        StudyRoom room = getById(id);
        StudyRoom update = new StudyRoom();
        update.setId(id);
        update.setStatus(status);
        baseMapper.updateById(update);
    }

    /**
     * 删除自习室（逻辑删除）
     * 如果自习室下存在座位则不允许删除
     */
    @Transactional
    public void delete(Long id) {
        getById(id);

        long seatCount = seatMapper.selectCount(
                new LambdaQueryWrapper<Seat>()
                        .eq(Seat::getRoomId, id)
        );
        if (seatCount > 0) {
            throw new BusinessException(2104, "自习室下存在 " + seatCount + " 个座位，无法删除");
        }

        baseMapper.deleteById(id);
    }

    /**
     * Excel批量导入自习室
     * <p>
     * 模板格式：校区名称 | 楼栋名称 | 楼层号 | 自习室名称 | 类型 | 开放时间 | 关闭时间 | 描述
     */
    @Transactional
    public Map<String, Object> importFromExcel(MultipartFile file) {
        int successCount = 0;
        int failCount = 0;
        List<Map<String, Object>> errors = new ArrayList<>();

        try (ExcelReader reader = ExcelUtil.getReader(file.getInputStream())) {
            List<Map<String, Object>> rows = reader.readAll();

            for (int i = 0; i < rows.size(); i++) {
                int rowNum = i + 2; // Excel行号（第1行是表头）
                Map<String, Object> row = rows.get(i);
                try {
                    String campusName = getCellString(row, "校区名称");
                    String buildingName = getCellString(row, "楼栋名称");
                    Integer floorNumber = getCellInteger(row, "楼层号");
                    String roomName = getCellString(row, "自习室名称");
                    String roomType = getCellString(row, "类型");
                    String openTimeStr = getCellString(row, "开放时间");
                    String closeTimeStr = getCellString(row, "关闭时间");
                    String description = getCellString(row, "描述");

                    // 校验必填字段
                    if (StrUtil.isBlank(campusName)) {
                        throw new IllegalArgumentException("校区名称为空");
                    }
                    if (StrUtil.isBlank(buildingName)) {
                        throw new IllegalArgumentException("楼栋名称为空");
                    }
                    if (floorNumber == null) {
                        throw new IllegalArgumentException("楼层号为空");
                    }
                    if (StrUtil.isBlank(roomName)) {
                        throw new IllegalArgumentException("自习室名称为空");
                    }

                    // 1. 查找或创建校区
                    Campus campus = findOrCreateCampus(campusName);

                    // 2. 查找或创建楼栋
                    Building building = findOrCreateBuilding(campus.getId(), buildingName);

                    // 3. 查找或创建楼层
                    Floor floor = findOrCreateFloor(building.getId(), floorNumber);

                    // 4. 检查同一楼层是否已存在同名自习室
                    Long existsCount = baseMapper.selectCount(
                            new LambdaQueryWrapper<StudyRoom>()
                                    .eq(StudyRoom::getFloorId, floor.getId())
                                    .eq(StudyRoom::getName, roomName)
                    );
                    if (existsCount > 0) {
                        throw new IllegalArgumentException("该楼层已存在同名自习室: " + roomName);
                    }

                    // 5. 创建自习室
                    StudyRoom room = new StudyRoom();
                    room.setFloorId(floor.getId());
                    room.setName(roomName);
                    room.setRoomType(StrUtil.isNotBlank(roomType) ? roomType : "LIBRARY");
                    room.setTotalSeats(0);
                    room.setRowsCount(0);
                    room.setColsCount(0);

                    if (StrUtil.isNotBlank(openTimeStr)) {
                        room.setOpenTime(LocalTime.parse(openTimeStr.trim()));
                    } else {
                        room.setOpenTime(LocalTime.of(8, 0));
                    }
                    if (StrUtil.isNotBlank(closeTimeStr)) {
                        room.setCloseTime(LocalTime.parse(closeTimeStr.trim()));
                    } else {
                        room.setCloseTime(LocalTime.of(22, 0));
                    }

                    room.setDescription(description);
                    room.setStatus(1);
                    baseMapper.insert(room);
                    successCount++;

                } catch (Exception e) {
                    failCount++;
                    Map<String, Object> error = new LinkedHashMap<>();
                    error.put("row", rowNum);
                    error.put("reason", e.getMessage());
                    errors.add(error);
                    log.warn("导入第{}行失败: {}", rowNum, e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new BusinessException(500, "读取Excel文件失败: " + e.getMessage());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);
        return result;
    }

    // ========== 私有辅助方法 ==========

    /**
     * 查找或创建校区
     */
    private Campus findOrCreateCampus(String name) {
        Campus existing = campusMapper.selectOne(
                new LambdaQueryWrapper<Campus>().eq(Campus::getName, name)
        );
        if (existing != null) {
            return existing;
        }
        Campus campus = new Campus();
        campus.setName(name);
        campus.setStatus(1);
        campus.setSortOrder(0);
        campusMapper.insert(campus);
        return campus;
    }

    /**
     * 查找或创建楼栋
     */
    private Building findOrCreateBuilding(Long campusId, String name) {
        Building existing = buildingMapper.selectOne(
                new LambdaQueryWrapper<Building>()
                        .eq(Building::getCampusId, campusId)
                        .eq(Building::getName, name)
        );
        if (existing != null) {
            return existing;
        }
        Building building = new Building();
        building.setCampusId(campusId);
        building.setName(name);
        building.setFloorCount(0);
        building.setStatus(1);
        building.setSortOrder(0);
        buildingMapper.insert(building);
        return building;
    }

    /**
     * 查找或创建楼层
     */
    private Floor findOrCreateFloor(Long buildingId, Integer floorNumber) {
        Floor existing = floorMapper.selectOne(
                new LambdaQueryWrapper<Floor>()
                        .eq(Floor::getBuildingId, buildingId)
                        .eq(Floor::getFloorNumber, floorNumber)
        );
        if (existing != null) {
            return existing;
        }
        Floor floor = new Floor();
        floor.setBuildingId(buildingId);
        floor.setFloorNumber(floorNumber);
        floor.setName(floorNumber + "楼");
        floor.setSortOrder(floorNumber);
        floor.setStatus(1);
        floorMapper.insert(floor);

        // 同步楼栋的楼层数
        long count = floorMapper.selectCount(
                new LambdaQueryWrapper<Floor>()
                        .eq(Floor::getBuildingId, buildingId)
        );
        Building building = new Building();
        building.setId(buildingId);
        building.setFloorCount((int) count);
        buildingMapper.updateById(building);

        return floor;
    }

    private String getCellString(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) {
            return null;
        }
        return value.toString().trim();
    }

    private Integer getCellInteger(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null || value.toString().isBlank()) {
            return null;
        }
        try {
            String s = value.toString().trim();
            // 处理科学计数法
            if (s.contains(".")) {
                return (int) Double.parseDouble(s);
            }
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(key + "不是有效数字: " + value);
        }
    }
}
