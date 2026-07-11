package com.studyroom.booking.modules.space.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.booking.common.ResultCode;
import com.studyroom.booking.common.exception.BusinessException;
import com.studyroom.booking.modules.space.dto.RoomSeatStatusVO;
import com.studyroom.booking.modules.space.dto.SeatGenerateRequest;
import com.studyroom.booking.modules.space.dto.SeatTagsUpdateRequest;
import com.studyroom.booking.modules.space.entity.Seat;
import com.studyroom.booking.modules.space.entity.StudyRoom;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyroom.booking.modules.reservation.entity.Booking;
import com.studyroom.booking.modules.reservation.mapper.BookingMapper;
import com.studyroom.booking.modules.space.mapper.SeatMapper;
import com.studyroom.booking.modules.space.mapper.StudyRoomMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 座位 Service
 *
 * @author 陈梦涵
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeatService extends ServiceImpl<SeatMapper, Seat> {

    private final StudyRoomMapper studyRoomMapper;
    private final BookingMapper bookingMapper;
    private final SeatUnavailableService seatUnavailableService;

    /**
     * 获取自习室下所有座位
     */
    public List<Seat> listByRoomId(Long roomId) {
        LambdaQueryWrapper<Seat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Seat::getRoomId, roomId);
        wrapper.orderByAsc(Seat::getRowNumber, Seat::getColNumber);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 根据ID获取座位
     */
    public Seat getById(Long id) {
        Seat seat = baseMapper.selectById(id);
        if (seat == null) {
            throw new BusinessException(ResultCode.SEAT_NOT_FOUND);
        }
        return seat;
    }

    /**
     * 批量生成座位
     * <p>
     * 按 rows × cols 网格生成座位，自动命名 A-01, A-02...，支持留空和特殊标签。
     * 会先删除该自习室下所有旧座位，然后重新生成。
     */
    @Transactional
    public List<Seat> generateSeats(Long roomId, SeatGenerateRequest request) {
        StudyRoom room = studyRoomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException(ResultCode.ROOM_NOT_FOUND);
        }

        int rows = request.getRows();
        int cols = request.getCols();

        // 构建留空位置 Set
        Set<String> emptySet = new HashSet<>();
        if (request.getEmptyPositions() != null) {
            for (SeatGenerateRequest.GridPosition p : request.getEmptyPositions()) {
                validatePosition(p.getRow(), p.getCol(), rows, cols, "留空位置");
                emptySet.add(p.getRow() + "-" + p.getCol());
            }
        }

        // 构建特殊标签 Map
        Map<String, String> specialMap = new LinkedHashMap<>();
        if (request.getSpecialPositions() != null) {
            for (SeatGenerateRequest.SpecialPosition sp : request.getSpecialPositions()) {
                validatePosition(sp.getRow(), sp.getCol(), rows, cols, "特殊标签位置");
                String key = sp.getRow() + "-" + sp.getCol();
                if (emptySet.contains(key)) {
                    throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "位置 (" + sp.getRow() + "," + sp.getCol() + ") 同时被设为留空和特殊标签，请检查");
                }
                if (sp.getTags() != null && !sp.getTags().isEmpty()) {
                    specialMap.put(key, String.join(",", sp.getTags()));
                }
            }
        }

        // 删除旧座位
        baseMapper.delete(new LambdaQueryWrapper<Seat>().eq(Seat::getRoomId, roomId));

        // 生成新座位
        List<Seat> seats = new ArrayList<>();
        for (int r = 1; r <= rows; r++) {
            String rowLabel = rowNumberToLabel(r);
            for (int c = 1; c <= cols; c++) {
                String positionKey = r + "-" + c;

                // 跳过留空位置
                if (emptySet.contains(positionKey)) {
                    continue;
                }

                Seat seat = new Seat();
                seat.setRoomId(roomId);
                seat.setSeatCode(rowLabel + "-" + String.format("%02d", c));
                seat.setRowNumber(r);
                seat.setColNumber(c);

                // 应用特殊标签
                String tags = specialMap.get(positionKey);
                seat.setTags(tags != null ? tags : "");

                seat.setStatus(1); // 默认可用
                seats.add(seat);
            }
        }

        // 批量插入
        if (!seats.isEmpty()) {
            saveBatch(seats, 500);
        }

        // 更新自习室元数据
        StudyRoom updateRoom = new StudyRoom();
        updateRoom.setId(roomId);
        updateRoom.setTotalSeats(seats.size());
        updateRoom.setRowsCount(rows);
        updateRoom.setColsCount(cols);
        studyRoomMapper.updateById(updateRoom);

        log.info("自习室 {} 生成座位完成: {}行×{}列, 实际生成{}个座位", roomId, rows, cols, seats.size());
        return seats;
    }

    /**
     * 更新座位信息
     */
    @Transactional
    public Seat updateSeat(Long id, String seatCode, Integer rowNumber, Integer colNumber,
                           String tags, Integer status) {
        Seat seat = getById(id);

        if (seatCode != null) {
            seat.setSeatCode(seatCode);
        }
        if (rowNumber != null) {
            seat.setRowNumber(rowNumber);
        }
        if (colNumber != null) {
            seat.setColNumber(colNumber);
        }
        if (tags != null) {
            seat.setTags(tags);
        }
        if (status != null) {
            seat.setStatus(status);
        }

        baseMapper.updateById(seat);
        return seat;
    }

    /**
     * 删除座位
     */
    @Transactional
    public void deleteSeat(Long id) {
        Seat seat = getById(id);
        baseMapper.deleteById(id);

        // 更新自习室的座位数
        updateRoomSeatCount(seat.getRoomId());
    }

    /**
     * 批量更新座位标签
     */
    @Transactional
    public void batchUpdateTags(Long roomId, SeatTagsUpdateRequest request) {
        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "座位ID列表不能为空");
        }

        // 验证所有座位都属于该自习室
        List<Seat> seats = baseMapper.selectList(
                new LambdaQueryWrapper<Seat>()
                        .eq(Seat::getRoomId, roomId)
                        .in(Seat::getId, request.getSeatIds())
        );

        Set<Long> foundIds = seats.stream().map(Seat::getId).collect(Collectors.toSet());
        List<Long> missingIds = request.getSeatIds().stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "以下座位不属于该自习室: " + missingIds);
        }

        String tags = request.getTags() != null ? String.join(",", request.getTags()) : "";

        List<Seat> updates = request.getSeatIds().stream().map(id -> {
            Seat s = new Seat();
            s.setId(id);
            s.setTags(tags);
            return s;
        }).toList();

        updateBatchById(updates);
    }

    /**
     * 获取自习室座位实时状态
     * <p>
     * 查询自习室所有座位，并结合当天的预约记录计算实时状态。
     * 此处通过直接查询 reservation 表来实现（与成员C的表交互）。
     */
    public RoomSeatStatusVO getRoomSeatStatus(Long roomId) {
        StudyRoom room = studyRoomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException(ResultCode.ROOM_NOT_FOUND);
        }

        // 查询所有座位
        List<Seat> seats = listByRoomId(roomId);

        RoomSeatStatusVO vo = new RoomSeatStatusVO();
        vo.setRoomId(roomId);
        vo.setRoomName(room.getName());
        vo.setTotalSeats(seats.size());

        List<RoomSeatStatusVO.SeatStatusItem> items = new ArrayList<>();
        int availableCount = 0;
        int reservedCount = 0;
        int occupiedCount = 0;

        for (Seat seat : seats) {
            RoomSeatStatusVO.SeatStatusItem item = new RoomSeatStatusVO.SeatStatusItem();
            item.setSeatId(seat.getId());
            item.setSeatCode(seat.getSeatCode());
            item.setRowNumber(seat.getRowNumber());
            item.setColNumber(seat.getColNumber());

            // 解析标签
            if (StrUtil.isNotBlank(seat.getTags())) {
                item.setTags(Arrays.asList(seat.getTags().split(",")));
            } else {
                item.setTags(Collections.emptyList());
            }

            // 判断状态
            if (seat.getStatus() == 0) {
                item.setStatus("UNAVAILABLE");
            } else if (seatUnavailableService.isSeatCurrentlyUnavailable(seat.getId())) {
                item.setStatus("UNAVAILABLE");
            } else {
                // 默认可预约，后续可通过关联预约表进一步细化
                // 精确的状态（RESERVED/OCCUPIED/TEMPORARY_LEAVE）需要查询预约表
                // 此处实现基础版本：座位物理可用 = AVAILABLE
                item.setStatus("AVAILABLE");
                availableCount++;
            }

            items.add(item);
        }

        vo.setSeats(items);
        vo.setAvailableSeats(availableCount);
        vo.setReservedSeats(reservedCount);
        vo.setOccupiedSeats(occupiedCount);

        return vo;
    }

    /**
     * 获取自习室座位在指定时段的状态
     * <p>
     * 查询该时段内所有有效预约记录，判断每个座位在该时段是否被占用。
     *
     * @param roomId    自习室ID
     * @param startTime 时段开始时间
     * @param endTime   时段结束时间
     */
    public RoomSeatStatusVO getRoomSeatStatusByTime(Long roomId,
                                                      LocalDateTime startTime,
                                                      LocalDateTime endTime) {
        log.info("getRoomSeatStatusByTime - roomId={}, startTime={}, endTime={}", roomId, startTime, endTime);
        
        // 查询该时段内的有效预约记录（时间重叠判断）
        List<Booking> bookings = bookingMapper.selectList(
                new LambdaQueryWrapper<Booking>()
                        .eq(Booking::getRoomId, roomId)
                        .in(Booking::getStatus, "RESERVED", "CHECKED_IN", "TEMPORARY_LEAVE")
                        .lt(Booking::getStartTime, endTime)
                        .gt(Booking::getEndTime, startTime)
        );

        // 构建 seatId -> status 映射（同座位有多条预约时，取优先级最高的状态）
        Map<Long, String> reservations = new HashMap<>();
        for (Booking booking : bookings) {
            Long seatId = booking.getSeatId();
            String status = booking.getStatus();
            String existing = reservations.get(seatId);
            // 优先级：CHECKED_IN > TEMPORARY_LEAVE > RESERVED
            if (existing == null || isHigherPriority(status, existing)) {
                reservations.put(seatId, status);
            }
        }

        return getRoomSeatStatusWithReservations(roomId, reservations, startTime, endTime);
    }

    private boolean isHigherPriority(String status, String existing) {
        int priority = switch (status) {
            case "CHECKED_IN" -> 3;
            case "TEMPORARY_LEAVE" -> 2;
            case "RESERVED" -> 1;
            default -> 0;
        };
        int existingPriority = switch (existing) {
            case "CHECKED_IN" -> 3;
            case "TEMPORARY_LEAVE" -> 2;
            case "RESERVED" -> 1;
            default -> 0;
        };
        return priority > existingPriority;
    }

    /**
     * 获取自习室座位实时状态（完整版，关联预约表）
     *
     * @param reservations 当天该自习室的有效预约记录
     *                     格式: Map<seatId, reservationStatus>
     *                     reservationStatus: RESERVED/CHECKED_IN/TEMPORARY_LEAVE
     */
    public RoomSeatStatusVO getRoomSeatStatusWithReservations(Long roomId,
                                                               Map<Long, String> reservations) {
        RoomSeatStatusVO vo = getRoomSeatStatus(roomId);

        int availableCount = 0;
        int reservedCount = 0;
        int occupiedCount = 0;

        for (RoomSeatStatusVO.SeatStatusItem item : vo.getSeats()) {
            if ("UNAVAILABLE".equals(item.getStatus())) {
                continue;
            }

            String resStatus = reservations.get(item.getSeatId());
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

        vo.setAvailableSeats(availableCount);
        vo.setReservedSeats(reservedCount);
        vo.setOccupiedSeats(occupiedCount);

        return vo;
    }

    public RoomSeatStatusVO getRoomSeatStatusWithReservations(Long roomId,
                                                               Map<Long, String> reservations,
                                                               LocalDateTime startTime,
                                                               LocalDateTime endTime) {
        StudyRoom room = studyRoomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException(ResultCode.ROOM_NOT_FOUND);
        }

        List<Seat> seats = listByRoomId(roomId);

        RoomSeatStatusVO vo = new RoomSeatStatusVO();
        vo.setRoomId(roomId);
        vo.setRoomName(room.getName());
        vo.setTotalSeats(seats.size());

        List<RoomSeatStatusVO.SeatStatusItem> items = new ArrayList<>();
        int availableCount = 0;
        int reservedCount = 0;
        int occupiedCount = 0;

        for (Seat seat : seats) {
            RoomSeatStatusVO.SeatStatusItem item = new RoomSeatStatusVO.SeatStatusItem();
            item.setSeatId(seat.getId());
            item.setSeatCode(seat.getSeatCode());
            item.setRowNumber(seat.getRowNumber());
            item.setColNumber(seat.getColNumber());

            if (StrUtil.isNotBlank(seat.getTags())) {
                item.setTags(Arrays.asList(seat.getTags().split(",")));
            } else {
                item.setTags(Collections.emptyList());
            }

            if (seat.getStatus() == 0) {
                item.setStatus("UNAVAILABLE");
            } else if (seatUnavailableService.isSeatUnavailableInPeriod(seat.getId(), startTime, endTime)) {
                item.setStatus("UNAVAILABLE");
            } else {
                String resStatus = reservations.get(seat.getId());
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
                        default:
                            item.setStatus("RESERVED");
                            reservedCount++;
                            break;
                    }
                }
            }

            items.add(item);
        }

        vo.setSeats(items);
        vo.setAvailableSeats(availableCount);
        vo.setReservedSeats(reservedCount);
        vo.setOccupiedSeats(occupiedCount);

        return vo;
    }

    // ========== 私有辅助方法 ==========

    /**
     * 行号转字母标签: 1→A, 2→B, ..., 26→Z, 27→AA, 28→AB...
     */
    private String rowNumberToLabel(int n) {
        StringBuilder sb = new StringBuilder();
        while (n > 0) {
            n--; // 转为0索引
            sb.insert(0, (char) ('A' + (n % 26)));
            n /= 26;
        }
        return sb.toString();
    }

    /**
     * 验证网格位置是否在有效范围内
     */
    private void validatePosition(Integer row, Integer col, int maxRows, int maxCols, String label) {
        if (row == null || col == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), label + "的行列号不能为空");
        }
        if (row < 1 || row > maxRows) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), label + "行号 " + row + " 超出范围 [1, " + maxRows + "]");
        }
        if (col < 1 || col > maxCols) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), label + "列号 " + col + " 超出范围 [1, " + maxCols + "]");
        }
    }

    /**
     * 更新自习室座位数
     */
    private void updateRoomSeatCount(Long roomId) {
        long count = baseMapper.selectCount(
                new LambdaQueryWrapper<Seat>()
                        .eq(Seat::getRoomId, roomId)
        );
        StudyRoom room = new StudyRoom();
        room.setId(roomId);
        room.setTotalSeats((int) count);
        studyRoomMapper.updateById(room);
    }
}
