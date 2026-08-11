package com.aditi.attendance.attendance.controller;

import com.aditi.attendance.attendance.dto.AttendanceRequest;
import com.aditi.attendance.attendance.dto.AttendanceResponse;
import com.aditi.attendance.attendance.dto.DashboardAttendanceResponse;
import com.aditi.attendance.attendance.dto.PeriodAttendanceReport;
import com.aditi.attendance.attendance.dto.PunchRequest;
import com.aditi.attendance.attendance.dto.TodayAttendanceReport;
import com.aditi.attendance.attendance.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    public AttendanceResponse createAttendance(
            @Valid @RequestBody AttendanceRequest request) {

        return attendanceService.createAttendance(request);
    }


    @PostMapping("/punch")
    public DashboardAttendanceResponse punch(
            @Valid @RequestBody PunchRequest request) {

        return attendanceService.punch(request);
    }


    @GetMapping("/dashboard")
    public DashboardAttendanceResponse getTodayDashboard(
            @RequestParam String email) {

        return attendanceService.getTodayDashboard(email);
    }

    @GetMapping("/reports/today")
    public TodayAttendanceReport getTodayReport(@RequestParam String email) {

        return attendanceService.getTodayReport(email);
    }

    @GetMapping("/reports/weekly")
    public PeriodAttendanceReport getWeeklyReport(@RequestParam String email) {

        return attendanceService.getWeeklyReport(email);
    }

    @GetMapping("/reports/monthly")
    public PeriodAttendanceReport getMonthlyReport(
            @RequestParam String email,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        return attendanceService.getMonthlyReport(email, year, month);
    }

    @GetMapping
    public List<AttendanceResponse> getAllAttendance() {

        return attendanceService.getAllAttendance();
    }

    @GetMapping("/{id}")
    public AttendanceResponse getAttendanceById(
            @PathVariable Long id) {

        return attendanceService.getAttendanceById(id);
    }

    @GetMapping("/employee/{employeeId}")
    public List<AttendanceResponse> getAttendanceByEmployee(
            @PathVariable Long employeeId) {

        return attendanceService.getAttendanceByEmployee(employeeId);
    }

    @GetMapping("/date")
    public List<AttendanceResponse> getAttendanceByDate(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate attendanceDate) {

        return attendanceService.getAttendanceByDate(attendanceDate);
    }

    @GetMapping("/search")
    public List<AttendanceResponse> searchAttendance(
            @RequestParam String keyword) {

        return attendanceService.searchAttendance(keyword);
    }

    @PutMapping("/{id}")
    public AttendanceResponse updateAttendance(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceRequest request) {

        return attendanceService.updateAttendance(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteAttendance(
            @PathVariable Long id) {

        attendanceService.deleteAttendance(id);
    }
}
