package com.aditi.attendance.attendance.mapper;

import com.aditi.attendance.attendance.dto.AttendanceRequest;
import com.aditi.attendance.attendance.dto.AttendanceResponse;
import com.aditi.attendance.entity.Attendance;
import com.aditi.attendance.entity.Employee;

import java.time.Duration;

public class AttendanceMapper {

    private AttendanceMapper() {
    }

    public static Attendance toEntity(AttendanceRequest request, Employee employee) {

        return Attendance.builder()
                .attendanceDate(request.getAttendanceDate())
                .checkInTime(request.getCheckInTime())
                .checkOutTime(request.getCheckOutTime())
                .workingHours(null)      // calculated in service
                .status(request.getStatus())
                .remarks(request.getRemarks())
                .employee(employee)
                .build();
    }

    public static AttendanceResponse toResponse(Attendance attendance) {

        return AttendanceResponse.builder()
                .id(attendance.getId())
                .employeeId(attendance.getEmployee().getId())
                .employeeCode(attendance.getEmployee().getEmployeeCode())
                .employeeName(
                        attendance.getEmployee().getFirstName()
                                + " "
                                + attendance.getEmployee().getLastName()
                )
                .attendanceDate(attendance.getAttendanceDate())
                .checkInTime(attendance.getCheckInTime())
                .checkOutTime(attendance.getCheckOutTime())
                .workingHours(attendance.getWorkingHours() == null
                        ? null
                        : Duration.ofMinutes(attendance.getWorkingHours()))
                .status(attendance.getStatus())
                .remarks(attendance.getRemarks())
                .build();
    }
}
