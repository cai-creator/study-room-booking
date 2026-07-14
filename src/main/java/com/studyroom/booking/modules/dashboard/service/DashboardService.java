package com.studyroom.booking.modules.dashboard.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyroom.booking.common.ResultCode;
import com.studyroom.booking.common.exception.BusinessException;
import com.studyroom.booking.modules.dashboard.dto.BuildingOverviewVO;
import com.studyroom.booking.modules.dashboard.dto.CampusOverviewVO;
import com.studyroom.booking.modules.dashboard.dto.RoomDetailVO;
import com.studyroom.booking.modules.dashboard.dto.RoomOverviewVO;
import com.studyroom.booking.modules.reservation.entity.Booking;
import com.studyroom.booking.modules.reservation.mapper.BookingMapper;
import com.studyroom.booking.modules.space.entity.*;
import com.studyroom.booking.modules.space.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 实时看板 Service
 * <p>
 * 提供校区、楼栋、自习室三级使用概览和座位实时状态查询。
 *
 * @author 郭学威
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CampusMapper campusMapper;
    private final BuildingMapper buildingMapper;
    private final FloorMapper floorMapper;
    private final StudyRoomMapper studyRoomMapper;
    private final SeatMapper seatMapper;
    private final BookingMapper bookingMapper;

    // ==================== 校区使用概览 ====================

    /**
     * 获取各校区自习室使用率概览
     * <p>
     * 统计每个校区下的自习室数、总座位数、当前可用座位数和使用率。
     * 当前活跃预约 = 状态为 RESERVED/CHECKED_IN/TEMPORARY_LEAVE 的预约。
     */
    public List<CampusOverviewVO> getCampusOverview() {
        // 1. 加载所有基础数据
        List<Campus> campuses = campusMapper.selectList(
                new LambdaQueryWrapper<Campus>().eq(Campus::getStatus, 1).orderByAsc(Campus::getSortOrder));
        List<Building> buildings = buildingMapper.selectList(
                new LambdaQueryWrapper<Building>().eq(Building::getStatus, 1));
        List<Floor> floors = floorMapper.selectList(
                new LambdaQueryWrapper<Floor>().eq(Floor::getStatus, 1));
        List<StudyRoom> rooms = studyRoomMapper.selectList(
                new LambdaQueryWrapper<StudyRoom>().ne(StudyRoom::getStatus, 0));

        // 构建层级关系 Map
        Map<Long, List<Building>> campusBuildings = buildings.stream()
                .collect(Collectors.groupingBy(Building::getCampusId));
        Map<Long, List<Floor>> buildingFloors = floors.stream()
                .collect(Collectors.groupingBy(Floor::getBuildingId));
        Map<Long, List<StudyRoom>> floorRooms = rooms.stream()
                .collect(Collectors.groupingBy(StudyRoom::getFloorId));

        // 2. 计算每个自习室的座位数
        List<Seat> allSeats = seatMapper.selectList(
                new LambdaQueryWrapper<Seat>().eq(Seat::getStatus, 1));
        Map<Long, Long> roomTotalSeats = allSeats.stream()
                .collect(Collectors.groupingBy(Seat::getRoomId, Collectors.counting()));

        // 3. 查询当前活跃预约（被占用的座位）
        List<Booking> activeBookings = getActiveBookings();
        Set<Long> occupiedSeatIds = activeBookings.stream()
                .map(Booking::getSeatId)
                .collect(Collectors.toSet());

        // 按自习室统计被占用座位数
        Map<Long, Long> roomOccupiedSeats = new HashMap<>();
        for (Seat seat : allSeats) {
            if (occupiedSeatIds.contains(seat.getId())) {
                roomOccupiedSeats.merge(seat.getRoomId(), 1L, Long::sum);
            }
        }

        // 4. 构建结果
        List<CampusOverviewVO> result = new ArrayList<>();
        for (Campus campus : campuses) {
            CampusOverviewVO vo = new CampusOverviewVO();
            vo.setCampusId(campus.getId());
            vo.setCampusName(campus.getName());

            int campusTotalRooms = 0;
            int campusTotalSeats = 0;
            int campusOccupiedSeats = 0;

            List<Building> campusBuildingList = campusBuildings.getOrDefault(campus.getId(), Collections.emptyList());
            for (Building building : campusBuildingList) {
                List<Floor> buildingFloorList = buildingFloors.getOrDefault(building.getId(), Collections.emptyList());
                for (Floor floor : buildingFloorList) {
                    List<StudyRoom> floorRoomList = floorRooms.getOrDefault(floor.getId(), Collections.emptyList());
                    for (StudyRoom room : floorRoomList) {
                        campusTotalRooms++;
                        int roomSeats = roomTotalSeats.getOrDefault(room.getId(), 0L).intValue();
                        campusTotalSeats += roomSeats;
                        campusOccupiedSeats += roomOccupiedSeats.getOrDefault(room.getId(), 0L).intValue();
                    }
                }
            }

            vo.setTotalRooms(campusTotalRooms);
            vo.setTotalSeats(campusTotalSeats);
            vo.setAvailableSeats(campusTotalSeats - campusOccupiedSeats);
            vo.setUsageRate(campusTotalSeats > 0
                    ? Math.round(campusOccupiedSeats * 10000.0 / campusTotalSeats) / 100.0
                    : 0.0);

            result.add(vo);
        }
        return result;
    }

    // ==================== 楼栋使用概览 ====================

    /**
     * 获取各楼栋自习室使用率概览
     * <p>
     * 可按校区过滤（campusId 可选）。
     */
    public List<BuildingOverviewVO> getBuildingOverview(Long campusId) {
        // 1. 加载基础数据
        List<Building> buildings;
        if (campusId != null) {
            buildings = buildingMapper.selectList(
                    new LambdaQueryWrapper<Building>()
                            .eq(Building::getCampusId, campusId)
                            .eq(Building::getStatus, 1)
                            .orderByAsc(Building::getSortOrder));
        } else {
            buildings = buildingMapper.selectList(
                    new LambdaQueryWrapper<Building>().eq(Building::getStatus, 1).orderByAsc(Building::getSortOrder));
        }

        List<Campus> campuses = campusMapper.selectList(null);
        Map<Long, Campus> campusMap = campuses.stream()
                .collect(Collectors.toMap(Campus::getId, c -> c, (a, b) -> a));

        List<Floor> floors = floorMapper.selectList(
                new LambdaQueryWrapper<Floor>().eq(Floor::getStatus, 1));
        Map<Long, List<Floor>> buildingFloors = floors.stream()
                .collect(Collectors.groupingBy(Floor::getBuildingId));

        List<StudyRoom> rooms = studyRoomMapper.selectList(
                new LambdaQueryWrapper<StudyRoom>().ne(StudyRoom::getStatus, 0));
        Map<Long, List<StudyRoom>> floorRooms = rooms.stream()
                .collect(Collectors.groupingBy(StudyRoom::getFloorId));

        // 2. 座位数统计
        List<Seat> allSeats = seatMapper.selectList(
                new LambdaQueryWrapper<Seat>().eq(Seat::getStatus, 1));
        Map<Long, Long> roomTotalSeats = allSeats.stream()
                .collect(Collectors.groupingBy(Seat::getRoomId, Collectors.counting()));

        // 3. 活跃预约
        List<Booking> activeBookings = getActiveBookings();
        Set<Long> occupiedSeatIds = activeBookings.stream()
                .map(Booking::getSeatId)
                .collect(Collectors.toSet());
        Map<Long, Long> roomOccupiedSeats = new HashMap<>();
        for (Seat seat : allSeats) {
            if (occupiedSeatIds.contains(seat.getId())) {
                roomOccupiedSeats.merge(seat.getRoomId(), 1L, Long::sum);
            }
        }

        // 4. 计算每个楼栋的统计
        List<BuildingOverviewVO> result = new ArrayList<>();
        for (Building building : buildings) {
            BuildingOverviewVO vo = new BuildingOverviewVO();
            vo.setBuildingId(building.getId());
            vo.setBuildingName(building.getName());
            vo.setCampusId(building.getCampusId());
            Campus campus = campusMap.get(building.getCampusId());
            vo.setCampusName(campus != null ? campus.getName() : "");

            int buildingTotalRooms = 0;
            int buildingTotalSeats = 0;
            int buildingOccupiedSeats = 0;

            List<Floor> buildingFloorList = buildingFloors.getOrDefault(building.getId(), Collections.emptyList());
            for (Floor floor : buildingFloorList) {
                List<StudyRoom> floorRoomList = floorRooms.getOrDefault(floor.getId(), Collections.emptyList());
                for (StudyRoom room : floorRoomList) {
                    buildingTotalRooms++;
                    int roomSeats = roomTotalSeats.getOrDefault(room.getId(), 0L).intValue();
                    buildingTotalSeats += roomSeats;
                    buildingOccupiedSeats += roomOccupiedSeats.getOrDefault(room.getId(), 0L).intValue();
                }
            }

            vo.setTotalRooms(buildingTotalRooms);
            vo.setTotalSeats(buildingTotalSeats);
            vo.setAvailableSeats(buildingTotalSeats - buildingOccupiedSeats);
            vo.setUsageRate(buildingTotalSeats > 0
                    ? Math.round(buildingOccupiedSeats * 10000.0 / buildingTotalSeats) / 100.0
                    : 0.0);

            result.add(vo);
        }
        return result;
    }

    // ==================== 自习室使用详情 ====================

    /**
     * 获取单个自习室的座位实时状态详情
     */
    public RoomDetailVO getRoomDetail(Long roomId) {
        StudyRoom room = studyRoomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException(ResultCode.ROOM_NOT_FOUND);
        }

        // 查询关联信息
        Floor floor = floorMapper.selectById(room.getFloorId());
        Building building = floor != null ? buildingMapper.selectById(floor.getBuildingId()) : null;
        Campus campus = building != null ? campusMapper.selectById(building.getCampusId()) : null;

        // 查询所有座位
        List<Seat> seats = seatMapper.selectList(
                new LambdaQueryWrapper<Seat>()
                        .eq(Seat::getRoomId, roomId)
                        .orderByAsc(Seat::getRowNumber, Seat::getColNumber));

        // 查询当前活跃预约（一次查询，复用两个派生结果）
        List<Booking> activeBookings = getActiveBookings();
        Set<Long> occupiedSeatIds = activeBookings.stream()
                .map(Booking::getSeatId)
                .collect(Collectors.toSet());

        // 需要区分 RESERVED / OCCUPIED / TEMPORARY_LEAVE
        Map<Long, String> activeReservationStatus = activeBookings.stream()
                .collect(Collectors.toMap(
                        Booking::getSeatId,
                        Booking::getStatus,
                        (existing, replacement) -> existing));

        // 构建VO
        RoomDetailVO vo = new RoomDetailVO();
        vo.setRoomId(roomId);
        vo.setRoomName(room.getName());
        vo.setRoomType(room.getRoomType());
        vo.setFloorName(floor != null ? floor.getName() : "");
        vo.setBuildingName(building != null ? building.getName() : "");
        vo.setCampusName(campus != null ? campus.getName() : "");
        vo.setOpenTime(room.getOpenTime() != null ? room.getOpenTime().toString() : "");
        vo.setCloseTime(room.getCloseTime() != null ? room.getCloseTime().toString() : "");

        int totalSeats = 0;
        int availableCount = 0;
        int reservedCount = 0;
        int occupiedCount = 0;
        List<RoomDetailVO.SeatStatusItem> items = new ArrayList<>();

        for (Seat seat : seats) {
            RoomDetailVO.SeatStatusItem item = new RoomDetailVO.SeatStatusItem();
            item.setSeatId(seat.getId());
            item.setSeatCode(seat.getSeatCode());
            item.setRowNumber(seat.getRowNumber());
            item.setColNumber(seat.getColNumber());

            // 标签
            if (StrUtil.isNotBlank(seat.getTags())) {
                item.setTags(Arrays.asList(seat.getTags().split(",")));
            } else {
                item.setTags(Collections.emptyList());
            }

            // 状态判断
            if (seat.getStatus() == 0) {
                item.setStatus("UNAVAILABLE");
            } else {
                totalSeats++;
                String resStatus = activeReservationStatus.get(seat.getId());
                if (resStatus == null) {
                    item.setStatus("AVAILABLE");
                    availableCount++;
                } else {
                    switch (resStatus) {
                        case "TEMPORARY_LEAVE":
                            item.setStatus("TEMPORARY_LEAVE");
                            occupiedCount++;
                            break;
                        case "CHECKED_IN":
                            item.setStatus("OCCUPIED");
                            occupiedCount++;
                            break;
                        default: // RESERVED
                            item.setStatus("RESERVED");
                            reservedCount++;
                            break;
                    }
                }
            }

            items.add(item);
        }

        vo.setSeats(items);
        vo.setTotalSeats(totalSeats);
        vo.setAvailableSeats(availableCount);
        vo.setReservedSeats(reservedCount);
        vo.setOccupiedSeats(occupiedCount);
        vo.setUsageRate(totalSeats > 0
                ? Math.round((totalSeats - availableCount) * 10000.0 / totalSeats) / 100.0
                : 0.0);

        return vo;
    }

    // ==================== 自习室使用概览（批量）====================

    /**
     * 获取所有自习室的使用概览
     * <p>
     * 批量查询所有自习室的座位总数、可用数、已预约数、已占用数和使用率。
     */
    public List<RoomOverviewVO> getRoomOverview() {
        // 1. 查询所有自习室
        List<StudyRoom> rooms = studyRoomMapper.selectList(
                new LambdaQueryWrapper<StudyRoom>().ne(StudyRoom::getStatus, 0));

        // 2. 查询所有座位并按自习室分组
        List<Seat> allSeats = seatMapper.selectList(
                new LambdaQueryWrapper<Seat>().eq(Seat::getStatus, 1));
        Map<Long, List<Seat>> seatsByRoom = allSeats.stream()
                .collect(Collectors.groupingBy(Seat::getRoomId));

        // 3. 查询当前活跃预约
        List<Booking> activeBookings = getActiveBookings();

        // 按座位ID统计各状态数量
        Map<Long, Long> reservedSeatCount = new HashMap<>();
        Map<Long, Long> occupiedSeatCount = new HashMap<>();
        for (Booking booking : activeBookings) {
            String status = booking.getStatus();
            Long seatId = booking.getSeatId();
            if ("RESERVED".equals(status)) {
                reservedSeatCount.merge(seatId, 1L, Long::sum);
            } else if ("CHECKED_IN".equals(status) || "TEMPORARY_LEAVE".equals(status)) {
                occupiedSeatCount.merge(seatId, 1L, Long::sum);
            }
        }

        // 4. 计算每个自习室的统计
        List<RoomOverviewVO> result = new ArrayList<>();
        for (StudyRoom room : rooms) {
            RoomOverviewVO vo = new RoomOverviewVO();
            vo.setRoomId(room.getId());
            vo.setRoomName(room.getName());
            vo.setFloorId(room.getFloorId());

            List<Seat> roomSeats = seatsByRoom.getOrDefault(room.getId(), Collections.emptyList());
            int totalSeats = roomSeats.size();

            int reservedCount = 0;
            int occupiedCount = 0;
            for (Seat seat : roomSeats) {
                if (reservedSeatCount.containsKey(seat.getId())) {
                    reservedCount++;
                } else if (occupiedSeatCount.containsKey(seat.getId())) {
                    occupiedCount++;
                }
            }

            int availableCount = totalSeats - reservedCount - occupiedCount;

            vo.setTotalSeats(totalSeats);
            vo.setAvailableSeats(availableCount);
            vo.setReservedSeats(reservedCount);
            vo.setOccupiedSeats(occupiedCount);
            vo.setUsageRate(totalSeats > 0
                    ? Math.round((reservedCount + occupiedCount) * 10000.0 / totalSeats) / 100.0
                    : 0.0);

            result.add(vo);
        }

        return result;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取当前活跃预约列表（一次查询，多处复用）
     * <p>
     * 活跃状态: RESERVED, CHECKED_IN, TEMPORARY_LEAVE
     */
    private List<Booking> getActiveBookings() {
        LocalDateTime now = LocalDateTime.now();
        return bookingMapper.selectList(
                new LambdaQueryWrapper<Booking>()
                        .in(Booking::getStatus, "RESERVED", "CHECKED_IN", "TEMPORARY_LEAVE")
                        .le(Booking::getStartTime, now)
                        .ge(Booking::getEndTime, now));
    }
}
