package com.aditi.attendance.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardAttendanceResponse {

    private Long employeeId;

    private String employeeName;

    private String email;

    private LocalDate attendanceDate;

    private String shiftStart;

    private String shiftEnd;

    private LocalTime firstCheckInTime;

    private LocalTime lastCheckOutTime;

    private Integer workingMinutes;

    private boolean currentlyCheckedIn;

    private Integer lastStatusId;

    @Builder.Default
    private List<CheckInOutPair> pairs = new ArrayList<>();
}
