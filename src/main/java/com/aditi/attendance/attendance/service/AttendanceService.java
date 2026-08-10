package com.aditi.attendance.attendance.service;

import com.aditi.attendance.attendance.dto.AttendanceRequest;
import com.aditi.attendance.attendance.dto.AttendanceResponse;
import com.aditi.attendance.attendance.exception.AttendanceNotFoundException;
import com.aditi.attendance.attendance.mapper.AttendanceMapper;
import com.aditi.attendance.attendance.repository.AttendanceCriteriaRepository;
import com.aditi.attendance.attendance.repository.AttendanceRepository;
import com.aditi.attendance.employee.exception.EmployeeNotFoundException;
import com.aditi.attendance.employee.repository.EmployeeRepository;
import com.aditi.attendance.entity.Attendance;
import com.aditi.attendance.entity.Employee;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceCriteriaRepository attendanceCriteriaRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public AttendanceResponse createAttendance(AttendanceRequest request) {

        Employee employee = findEmployeeById(request.getEmployeeId());
        Attendance attendance = AttendanceMapper.toEntity(request, employee);

        Attendance savedAttendance = attendanceRepository.save(attendance);

        // Recalculate working hours for all events of this employee on the same date
        recalculateWorkingHoursForDate(employee.getId(), request.getAttendanceDate());

        return AttendanceMapper.toResponse(savedAttendance);
    }

        public List<AttendanceResponse> getAllAttendance() {

                List<Attendance> all = attendanceRepository.findAll();

                return all.stream()
                                .collect(Collectors.groupingBy(a -> a.getEmployee().getId() + "|" + a.getAttendanceDate()))
                                .values()
                                .stream()
                                .map(this::aggregateDay)
                                .collect(Collectors.toList());
        }

    public AttendanceResponse getAttendanceById(Long id) {

        Attendance attendance = findAttendanceById(id);

        return AttendanceMapper.toResponse(attendance);
    }

        public List<AttendanceResponse> getAttendanceByEmployee(Long employeeId) {

                findEmployeeById(employeeId);

                List<Attendance> events = attendanceRepository.findByEmployeeId(employeeId);

                return events.stream()
                                .collect(Collectors.groupingBy(Attendance::getAttendanceDate))
                                .values()
                                .stream()
                                .map(this::aggregateDay)
                                .collect(Collectors.toList());
        }

        public List<AttendanceResponse> getAttendanceByDate(LocalDate attendanceDate) {

                List<Attendance> events = attendanceRepository.findByAttendanceDate(attendanceDate);

                return events.stream()
                                .collect(Collectors.groupingBy(a -> a.getEmployee().getId()))
                                .values()
                                .stream()
                                .map(this::aggregateDay)
                                .collect(Collectors.toList());
        }

    @Transactional
    public AttendanceResponse updateAttendance(Long id, AttendanceRequest request) {

        Attendance attendance = findAttendanceById(id);

        Employee employee = findEmployeeById(request.getEmployeeId());
        attendance.setEmployee(employee);
        attendance.setAttendanceDate(request.getAttendanceDate());
        attendance.setCheckInTime(request.getCheckInTime());
        attendance.setCheckOutTime(request.getCheckOutTime());
        attendance.setStatus(request.getStatus());
        attendance.setRemarks(request.getRemarks());

        Attendance updatedAttendance = attendanceRepository.save(attendance);

        // Recalculate working hours for all events of this employee on the same date
        recalculateWorkingHoursForDate(employee.getId(), request.getAttendanceDate());

        return AttendanceMapper.toResponse(updatedAttendance);
    }

    @Transactional
    public void deleteAttendance(Long id) {

        Attendance attendance = findAttendanceById(id);

        attendanceRepository.delete(attendance);
    }

    public List<AttendanceResponse> searchAttendance(String keyword) {

        return attendanceCriteriaRepository.searchAttendance(keyword)
                .stream()
                .map(AttendanceMapper::toResponse)
                .collect(Collectors.toList());
    }

    private Attendance findAttendanceById(Long id) {

        return attendanceRepository.findById(id)
                .orElseThrow(() ->
                        new AttendanceNotFoundException(
                                "Attendance with ID " + id + " not found."
                        ));
    }

    private Employee findEmployeeById(Long employeeId) {

        return employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new EmployeeNotFoundException(
                                "Employee with ID " + employeeId + " not found."
                        ));
    }
    private Integer calculateWorkingHours(LocalTime checkInTime,
                                          LocalTime checkOutTime) {

        if (checkInTime == null || checkOutTime == null) {
            return null;
        }

        if (checkOutTime.isBefore(checkInTime)) {
            throw new IllegalArgumentException(
                    "Check-out time cannot be before check-in time."
            );
        }

        return (int) Duration
                .between(checkInTime, checkOutTime)
                .toMinutes();
    }

    private AttendanceResponse aggregateDay(List<Attendance> events) {

        // earliest check-in
        LocalTime earliestIn = events.stream()
                .map(Attendance::getCheckInTime)
                .filter(t -> t != null)
                .min(LocalTime::compareTo)
                .orElse(null);

        // latest check-out
        LocalTime latestOut = events.stream()
                .map(Attendance::getCheckOutTime)
                .filter(t -> t != null)
                .max(LocalTime::compareTo)
                .orElse(null);

        // determine latest status by latest timestamp among checkIn/checkOut
        Attendance latestEvent = events.stream()
                .max((a, b) -> {
                    LocalTime ta = a.getCheckOutTime() != null ? a.getCheckOutTime() : a.getCheckInTime();
                    LocalTime tb = b.getCheckOutTime() != null ? b.getCheckOutTime() : b.getCheckInTime();
                    if (ta == null && tb == null) return 0;
                    if (ta == null) return -1;
                    if (tb == null) return 1;
                    return ta.compareTo(tb);
                })
                .orElse(events.get(0));

        Integer minutes = calculateWorkingHours(earliestIn, latestOut);

        Attendance sample = events.get(0);

        return AttendanceResponse.builder()
                .id(sample.getId())
                .employeeId(sample.getEmployee().getId())
                .employeeCode(sample.getEmployee().getEmployeeCode())
                .employeeName(sample.getEmployee().getFirstName() + " " + sample.getEmployee().getLastName())
                .attendanceDate(sample.getAttendanceDate())
                .checkInTime(earliestIn)
                .checkOutTime(latestOut)
                .workingHours(minutes == null ? null : Duration.ofMinutes(minutes))
                .status(latestEvent.getStatus())
                .remarks(latestEvent.getRemarks())
                .build();
    }

    private void recalculateWorkingHoursForDate(Long employeeId, LocalDate attendanceDate) {

        List<Attendance> events = attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, attendanceDate);

        if (events == null || events.isEmpty()) return;

        LocalTime earliestIn = events.stream()
                .map(Attendance::getCheckInTime)
                .filter(t -> t != null)
                .min(LocalTime::compareTo)
                .orElse(null);

        LocalTime latestOut = events.stream()
                .map(Attendance::getCheckOutTime)
                .filter(t -> t != null)
                .max(LocalTime::compareTo)
                .orElse(null);

        Integer minutes = calculateWorkingHours(earliestIn, latestOut);

        for (Attendance ev : events) {
            ev.setWorkingHours(minutes);
            attendanceRepository.save(ev);
        }
    }

}