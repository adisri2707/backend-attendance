package com.aditi.attendance.attendance.service;

import com.aditi.attendance.attendance.dto.AttendanceRequest;
import com.aditi.attendance.attendance.dto.AttendanceResponse;
import com.aditi.attendance.attendance.dto.CheckInOutPair;
import com.aditi.attendance.attendance.dto.DashboardAttendanceResponse;
import com.aditi.attendance.attendance.dto.DayAttendanceStatus;
import com.aditi.attendance.attendance.dto.EmployeeAttendanceSummary;
import com.aditi.attendance.attendance.dto.PeriodAttendanceReport;
import com.aditi.attendance.attendance.dto.PunchRequest;
import com.aditi.attendance.attendance.dto.TodayAttendanceItem;
import com.aditi.attendance.attendance.dto.TodayAttendanceReport;
import com.aditi.attendance.attendance.exception.AttendanceNotFoundException;
import com.aditi.attendance.attendance.exception.InvalidAttendanceActionException;
import com.aditi.attendance.attendance.mapper.AttendanceMapper;
import com.aditi.attendance.attendance.repository.AttendanceCriteriaRepository;
import com.aditi.attendance.attendance.repository.AttendanceRepository;
import com.aditi.attendance.common.util.Constants;
import com.aditi.attendance.employee.exception.EmployeeNotFoundException;
import com.aditi.attendance.employee.repository.EmployeeRepository;
import com.aditi.attendance.entity.Attendance;
import com.aditi.attendance.entity.Employee;
import com.aditi.attendance.entity.User;
import com.aditi.attendance.user.exception.UserNotFoundException;
import com.aditi.attendance.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceCriteriaRepository attendanceCriteriaRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @Transactional
    public AttendanceResponse createAttendance(AttendanceRequest request) {

        Employee employee = findEmployeeById(request.getEmployeeId());
        Attendance attendance = AttendanceMapper.toEntity(request, employee);

        Attendance savedAttendance = attendanceRepository.save(attendance);

        recalculateWorkingHoursForDate(employee.getId(), request.getAttendanceDate());

        return AttendanceMapper.toResponse(savedAttendance);
    }

    @Transactional
    public DashboardAttendanceResponse punch(PunchRequest request) {

        int statusId = request.getStatusId();
        if (statusId != Constants.STATUS_CHECK_IN && statusId != Constants.STATUS_CHECK_OUT) {
            throw new InvalidAttendanceActionException(
                    "Status id must be 1 (check-in) or 2 (check-out)."
            );
        }

        Employee employee = employeeRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EmployeeNotFoundException(
                        "Employee not found for email: " + request.getEmail()
                ));

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now().withNano(0);

        List<Attendance> todayEvents = sortedEvents(
                attendanceRepository.findByEmployeeIdAndAttendanceDate(employee.getId(), today)
        );

        Integer lastStatusId = todayEvents.isEmpty()
                ? null
                : resolveStatusId(todayEvents.get(todayEvents.size() - 1).getStatus());

        if (statusId == Constants.STATUS_CHECK_IN) {
            if (lastStatusId != null && lastStatusId == Constants.STATUS_CHECK_IN) {
                throw new InvalidAttendanceActionException(
                        "Already checked in. Please check out first."
                );
            }
        } else {
            if (lastStatusId == null || lastStatusId != Constants.STATUS_CHECK_IN) {
                throw new InvalidAttendanceActionException(
                        "Cannot check out before checking in."
                );
            }
        }


        Attendance punch = Attendance.builder()
                .employee(employee)
                .attendanceDate(today)
                .checkInTime(statusId == Constants.STATUS_CHECK_IN ? now : null)
                .checkOutTime(statusId == Constants.STATUS_CHECK_OUT ? now : null)
                .status(String.valueOf(statusId))
                .workingHours(null)
                .remarks(null)
                .build();

        attendanceRepository.save(punch);
        recalculateWorkingHoursForDate(employee.getId(), today);

        return buildDashboardResponse(employee, today);
    }

    public DashboardAttendanceResponse getTodayDashboard(String email) {

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new EmployeeNotFoundException(
                        "Employee not found for email: " + email
                ));

        return buildDashboardResponse(employee, LocalDate.now());
    }

    @Transactional
    public TodayAttendanceReport getTodayReport(String email) {

        User user = findUserByEmail(email);
        boolean adminView = isAdmin(user);
        LocalDate today = LocalDate.now();

        List<Employee> employees = resolveEmployeesForReport(user, adminView);

        List<TodayAttendanceItem> items = employees.stream()
                .filter(employee -> {
                    LocalDate joining = employee.getDateOfJoining();
                    return joining == null || !today.isBefore(joining);
                })
                .map(employee -> toTodayItem(employee, today))
                .sorted(Comparator.comparing(TodayAttendanceItem::getEmployeeName,
                        Comparator.nullsLast(String::compareToIgnoreCase)))
                .collect(Collectors.toList());

        return TodayAttendanceReport.builder()
                .adminView(adminView)
                .date(today)
                .items(items)
                .build();
    }

    @Transactional
    public PeriodAttendanceReport getWeeklyReport(String email) {

        User user = findUserByEmail(email);
        boolean adminView = isAdmin(user);

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        LocalDate effectiveEnd = weekEnd.isAfter(today) ? today : weekEnd;

        return buildPeriodReport(user, adminView, "WEEKLY", weekStart, effectiveEnd, today.getYear(), today.getMonthValue());
    }

    @Transactional
    public PeriodAttendanceReport getMonthlyReport(String email, Integer year, Integer month) {

        User user = findUserByEmail(email);
        boolean adminView = isAdmin(user);

        LocalDate today = LocalDate.now();
        int reportYear = year != null ? year : today.getYear();
        int reportMonth = month != null ? month : today.getMonthValue();

        LocalDate monthStart = LocalDate.of(reportYear, reportMonth, 1);
        LocalDate monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth());
        LocalDate effectiveEnd = monthEnd.isAfter(today) ? today : monthEnd;

        return buildPeriodReport(user, adminView, "MONTHLY", monthStart, effectiveEnd, reportYear, reportMonth);
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

    private PeriodAttendanceReport buildPeriodReport(
            User user,
            boolean adminView,
            String periodType,
            LocalDate startDate,
            LocalDate endDate,
            int year,
            int month) {

        List<LocalDate> workingDays = workingDaysBetween(startDate, endDate);
        List<Employee> employees = resolveEmployeesForReport(user, adminView);

        List<EmployeeAttendanceSummary> summaries = new ArrayList<>();
        int totalPresent = 0;
        int totalAbsent = 0;

        for (Employee employee : employees) {
            EmployeeAttendanceSummary summary = summarizeEmployee(employee, workingDays);
            summaries.add(summary);
            totalPresent += summary.getPresentDays();
            totalAbsent += summary.getAbsentDays();
        }

        summaries.sort(Comparator.comparing(EmployeeAttendanceSummary::getEmployeeName,
                Comparator.nullsLast(String::compareToIgnoreCase)));

        return PeriodAttendanceReport.builder()
                .adminView(adminView)
                .periodType(periodType)
                .startDate(startDate)
                .endDate(endDate)
                .year(year)
                .month(month)
                .totalPresent(totalPresent)
                .totalAbsent(totalAbsent)
                .employees(summaries)
                .build();
    }

    private EmployeeAttendanceSummary summarizeEmployee(Employee employee, List<LocalDate> workingDays) {

        // Count attendance only from date of joining onward (not before joining).
        LocalDate joiningDate = employee.getDateOfJoining();
        List<LocalDate> applicableDays = workingDays.stream()
                .filter(day -> joiningDate == null || !day.isBefore(joiningDate))
                .collect(Collectors.toList());

        if (applicableDays.isEmpty()) {
            return EmployeeAttendanceSummary.builder()
                    .employeeId(employee.getId())
                    .employeeCode(employee.getEmployeeCode())
                    .employeeName(fullName(employee))
                    .presentDays(0)
                    .absentDays(0)
                    .days(new ArrayList<>())
                    .build();
        }

        LocalDate rangeStart = applicableDays.get(0);
        LocalDate rangeEnd = applicableDays.get(applicableDays.size() - 1);

        Map<LocalDate, List<Attendance>> byDate = attendanceRepository
                .findByEmployeeIdAndAttendanceDateBetween(employee.getId(), rangeStart, rangeEnd)
                .stream()
                .collect(Collectors.groupingBy(Attendance::getAttendanceDate));

        List<DayAttendanceStatus> days = new ArrayList<>();
        int present = 0;
        int absent = 0;

        for (LocalDate day : applicableDays) {
            List<Attendance> events = byDate.getOrDefault(day, List.of());
            DayAttendanceStatus dayStatus = toDayStatus(day, events);
            days.add(dayStatus);
            if ("PRESENT".equals(dayStatus.getStatus())) {
                present++;
            } else {
                absent++;
            }
        }

        return EmployeeAttendanceSummary.builder()
                .employeeId(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .employeeName(fullName(employee))
                .presentDays(present)
                .absentDays(absent)
                .days(days)
                .build();
    }

    private DayAttendanceStatus toDayStatus(LocalDate date, List<Attendance> events) {

        LocalTime firstIn = firstCheckIn(events);
        LocalTime lastOut = lastCheckOut(events);
        boolean present = firstIn != null;

        Integer minutes = null;
        if (firstIn != null && lastOut != null) {
            minutes = calculateWorkingMinutes(firstIn, lastOut);
        } else if (firstIn != null && date.equals(LocalDate.now())) {
            minutes = calculateWorkingMinutes(firstIn, LocalTime.now().withNano(0));
        }

        return DayAttendanceStatus.builder()
                .date(date)
                .status(present ? "PRESENT" : "ABSENT")
                .checkInTime(firstIn)
                .checkOutTime(lastOut)
                .workingMinutes(minutes)
                .build();
    }

    private TodayAttendanceItem toTodayItem(Employee employee, LocalDate date) {

        List<Attendance> events = attendanceRepository
                .findByEmployeeIdAndAttendanceDate(employee.getId(), date);

        LocalTime firstIn = firstCheckIn(events);
        LocalTime lastOut = lastCheckOut(events);
        boolean present = firstIn != null;

        Integer minutes = null;
        if (firstIn != null) {
            if (lastOut != null) {
                minutes = calculateWorkingMinutes(firstIn, lastOut);
            } else if (isCurrentlyCheckedIn(events)) {
                minutes = calculateWorkingMinutes(firstIn, LocalTime.now().withNano(0));
            }
        }

        return TodayAttendanceItem.builder()
                .employeeId(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .employeeName(fullName(employee))
                .attendanceDate(date)
                .checkInTime(firstIn)
                .checkOutTime(lastOut)
                .workingMinutes(minutes)
                .dayStatus(present ? "PRESENT" : "ABSENT")
                .build();
    }

    private boolean isCurrentlyCheckedIn(List<Attendance> events) {
        List<Attendance> sorted = sortedEvents(events);
        if (sorted.isEmpty()) {
            return false;
        }
        Integer lastStatusId = resolveStatusId(sorted.get(sorted.size() - 1).getStatus());
        return lastStatusId != null && lastStatusId == Constants.STATUS_CHECK_IN;
    }

    private LocalTime firstCheckIn(List<Attendance> events) {
        return events.stream()
                .filter(e -> isCheckIn(e.getStatus()))
                .map(this::resolveEventTime)
                .filter(t -> t != null)
                .min(LocalTime::compareTo)
                .orElseGet(() -> events.stream()
                        .map(Attendance::getCheckInTime)
                        .filter(t -> t != null)
                        .min(LocalTime::compareTo)
                        .orElse(null));
    }

    private LocalTime lastCheckOut(List<Attendance> events) {
        return events.stream()
                .filter(e -> isCheckOut(e.getStatus()))
                .map(this::resolveEventTime)
                .filter(t -> t != null)
                .max(LocalTime::compareTo)
                .orElseGet(() -> events.stream()
                        .map(Attendance::getCheckOutTime)
                        .filter(t -> t != null)
                        .max(LocalTime::compareTo)
                        .orElse(null));
    }

    private List<LocalDate> workingDaysBetween(LocalDate start, LocalDate end) {
        List<LocalDate> days = new ArrayList<>();
        if (start == null || end == null || end.isBefore(start)) {
            return days;
        }
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            DayOfWeek dow = d.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                days.add(d);
            }
        }
        return days;
    }

    private List<Employee> resolveEmployeesForReport(User user, boolean adminView) {
        if (adminView) {
            return employeeRepository.findByDeletedFalse().stream()
                    .filter(e -> Boolean.TRUE.equals(e.getActive()))
                    .sorted(Comparator.comparing(this::fullName, String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList());
        }
        return List.of(user.getEmployee());
    }

    private User findUserByEmail(String email) {
        return userRepository.findByUsername(email)
                .or(() -> userRepository.findByEmployeeEmail(email))
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found for email: " + email
                ));
    }

    private boolean isAdmin(User user) {
        return user.getRole() != null
                && Constants.ROLE_ADMIN.equalsIgnoreCase(user.getRole().getRoleName());
    }

    private String fullName(Employee employee) {
        String first = employee.getFirstName() != null ? employee.getFirstName() : "";
        String last = employee.getLastName() != null ? employee.getLastName() : "";
        return (first + " " + last).trim();
    }

    private DashboardAttendanceResponse buildDashboardResponse(Employee employee, LocalDate date) {

        List<Attendance> events = sortedEvents(
                attendanceRepository.findByEmployeeIdAndAttendanceDate(employee.getId(), date)
        );

        LocalTime firstCheckIn = events.stream()
                .filter(e -> isCheckIn(e.getStatus()))
                .map(this::resolveEventTime)
                .filter(t -> t != null)
                .min(LocalTime::compareTo)
                .orElse(null);

        LocalTime lastCheckOut = events.stream()
                .filter(e -> isCheckOut(e.getStatus()))
                .map(this::resolveEventTime)
                .filter(t -> t != null)
                .max(LocalTime::compareTo)
                .orElse(null);

        Integer lastStatusId = events.isEmpty()
                ? null
                : resolveStatusId(events.get(events.size() - 1).getStatus());

        boolean currentlyCheckedIn = lastStatusId != null
                && lastStatusId == Constants.STATUS_CHECK_IN;

        Integer workingMinutes = null;
        if (firstCheckIn != null) {
            LocalTime end = currentlyCheckedIn ? LocalTime.now().withNano(0) : lastCheckOut;
            if (end != null) {
                workingMinutes = calculateWorkingMinutes(firstCheckIn, end);
            }
        }

        return DashboardAttendanceResponse.builder()
                .employeeId(employee.getId())
                .employeeName(employee.getFirstName() + " " + employee.getLastName())
                .email(employee.getEmail())
                .attendanceDate(date)
                .shiftStart(Constants.SHIFT_START)
                .shiftEnd(Constants.SHIFT_END)
                .firstCheckInTime(firstCheckIn)
                .lastCheckOutTime(lastCheckOut)
                .workingMinutes(workingMinutes)
                .currentlyCheckedIn(currentlyCheckedIn)
                .lastStatusId(lastStatusId)
                .pairs(buildPairs(events))
                .build();
    }

    private List<CheckInOutPair> buildPairs(List<Attendance> events) {

        List<CheckInOutPair> pairs = new ArrayList<>();
        LocalTime openCheckIn = null;

        for (Attendance event : events) {
            if (isCheckIn(event.getStatus())) {
                if (openCheckIn != null) {
                    pairs.add(CheckInOutPair.builder()
                            .checkInTime(openCheckIn)
                            .checkOutTime(null)
                            .build());
                }
                openCheckIn = resolveEventTime(event);
            } else if (isCheckOut(event.getStatus())) {
                pairs.add(CheckInOutPair.builder()
                        .checkInTime(openCheckIn)
                        .checkOutTime(resolveEventTime(event))
                        .build());
                openCheckIn = null;
            }
        }

        if (openCheckIn != null) {
            pairs.add(CheckInOutPair.builder()
                    .checkInTime(openCheckIn)
                    .checkOutTime(null)
                    .build());
        }

        return pairs;
    }

    private List<Attendance> sortedEvents(List<Attendance> events) {

        return events.stream()
                .sorted(Comparator
                        .comparing((Attendance a) -> resolveEventTime(a),
                                Comparator.nullsLast(LocalTime::compareTo))
                        .thenComparing(Attendance::getId, Comparator.nullsLast(Long::compareTo)))
                .collect(Collectors.toList());
    }

    private LocalTime resolveEventTime(Attendance attendance) {

        if (isCheckIn(attendance.getStatus())) {
            return attendance.getCheckInTime();
        }
        if (isCheckOut(attendance.getStatus())) {
            return attendance.getCheckOutTime() != null
                    ? attendance.getCheckOutTime()
                    : attendance.getCheckInTime();
        }
        if (attendance.getCheckOutTime() != null) {
            return attendance.getCheckOutTime();
        }
        return attendance.getCheckInTime();
    }

    private boolean isCheckIn(String status) {
        Integer id = resolveStatusId(status);
        return id != null && id == Constants.STATUS_CHECK_IN;
    }

    private boolean isCheckOut(String status) {
        Integer id = resolveStatusId(status);
        return id != null && id == Constants.STATUS_CHECK_OUT;
    }

    private Integer resolveStatusId(String status) {

        if (status == null) {
            return null;
        }
        if ("1".equals(status) || "CHECK_IN".equalsIgnoreCase(status)) {
            return Constants.STATUS_CHECK_IN;
        }
        if ("2".equals(status) || "CHECK_OUT".equalsIgnoreCase(status)) {
            return Constants.STATUS_CHECK_OUT;
        }
        return null;
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

    private Integer calculateWorkingMinutes(LocalTime checkInTime, LocalTime checkOutTime) {

        if (checkInTime == null || checkOutTime == null) {
            return null;
        }

        if (checkOutTime.isBefore(checkInTime)) {
            throw new InvalidAttendanceActionException(
                    "Check-out time cannot be before check-in time."
            );
        }

        return (int) Duration.between(checkInTime, checkOutTime).toMinutes();
    }

    private AttendanceResponse aggregateDay(List<Attendance> events) {

        List<Attendance> sorted = sortedEvents(events);

        LocalTime earliestIn = sorted.stream()
                .filter(e -> isCheckIn(e.getStatus()) || e.getCheckInTime() != null)
                .map(e -> isCheckIn(e.getStatus()) ? resolveEventTime(e) : e.getCheckInTime())
                .filter(t -> t != null)
                .min(LocalTime::compareTo)
                .orElse(null);

        LocalTime latestOut = sorted.stream()
                .filter(e -> isCheckOut(e.getStatus()) || e.getCheckOutTime() != null)
                .map(e -> isCheckOut(e.getStatus()) ? resolveEventTime(e) : e.getCheckOutTime())
                .filter(t -> t != null)
                .max(LocalTime::compareTo)
                .orElse(null);

        Attendance latestEvent = sorted.isEmpty() ? events.get(0) : sorted.get(sorted.size() - 1);

        Integer minutes = calculateWorkingMinutes(earliestIn, latestOut);

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

        if (events == null || events.isEmpty()) {
            return;
        }

        LocalTime earliestIn = events.stream()
                .filter(e -> isCheckIn(e.getStatus()) || e.getCheckInTime() != null)
                .map(e -> isCheckIn(e.getStatus()) ? resolveEventTime(e) : e.getCheckInTime())
                .filter(t -> t != null)
                .min(LocalTime::compareTo)
                .orElse(null);

        LocalTime latestOut = events.stream()
                .filter(e -> isCheckOut(e.getStatus()) || e.getCheckOutTime() != null)
                .map(e -> isCheckOut(e.getStatus()) ? resolveEventTime(e) : e.getCheckOutTime())
                .filter(t -> t != null)
                .max(LocalTime::compareTo)
                .orElse(null);

        Integer minutes = null;
        if (earliestIn != null && latestOut != null) {
            minutes = calculateWorkingMinutes(earliestIn, latestOut);
        }

        for (Attendance ev : events) {
            ev.setWorkingHours(minutes);
            attendanceRepository.save(ev);
        }
    }
}
