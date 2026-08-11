package com.aditi.attendance.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeAttendanceSummary {

    private Long employeeId;

    private String employeeCode;

    private String employeeName;

    private int presentDays;

    private int absentDays;

    @Builder.Default
    private List<DayAttendanceStatus> days = new ArrayList<>();
}
