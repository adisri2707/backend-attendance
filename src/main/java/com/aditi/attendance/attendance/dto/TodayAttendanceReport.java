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
public class TodayAttendanceReport {

    private boolean adminView;

    private LocalDate date;

    @Builder.Default
    private List<TodayAttendanceItem> items = new ArrayList<>();
}
