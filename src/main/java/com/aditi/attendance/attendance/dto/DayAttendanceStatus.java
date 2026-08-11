package com.aditi.attendance.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DayAttendanceStatus {

    private LocalDate date;

    private String status;

    private LocalTime checkInTime;

    private LocalTime checkOutTime;

    private Integer workingMinutes;
}
