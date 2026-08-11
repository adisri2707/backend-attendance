package com.aditi.attendance.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodAttendanceReport {

    private boolean adminView;

    private String periodType;

    private LocalDate startDate;

    private LocalDate endDate;

    private int year;

    private int month;

    private int totalPresent;

    private int totalAbsent;

    @Builder.Default
    private List<EmployeeAttendanceSummary> employees = new ArrayList<>();
}
