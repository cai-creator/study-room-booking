package com.studyroom.booking.modules.seat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 座位管控定时任务配置
 * <p>
 * 启用 Spring 定时任务支持，用于座位管控模块的自动化处理。
 *
 * @author 邓祺然
 */
@Configuration
@EnableScheduling
public class SeatSchedulingConfig {
}
