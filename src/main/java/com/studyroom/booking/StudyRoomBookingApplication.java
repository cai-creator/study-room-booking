package com.studyroom.booking;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.studyroom.booking.modules.*.mapper")
public class StudyRoomBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyRoomBookingApplication.class, args);
    }
}
