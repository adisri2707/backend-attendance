package com.aditi.attendance.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class AttendanceSchemaFixer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        allowNullableCheckInTime();
        allowMultiplePunchesPerDay();
    }

    private void allowNullableCheckInTime() {
        try {
            jdbcTemplate.execute("ALTER TABLE attendance MODIFY check_in_time TIME NULL");
            log.info("Ensured attendance.check_in_time is nullable");
        } catch (Exception ex) {
            log.warn("Could not alter attendance.check_in_time: {}", ex.getMessage());
        }
    }

    private void allowMultiplePunchesPerDay() {
        try {
            if (!hasUniqueEmployeeDateIndex()) {
                ensureNonUniqueIndexesAndFk();
                log.info("Attendance already allows multiple punches per day");
                return;
            }

            String fkName = findEmployeeForeignKeyName();
            String uniqueIndex = findUniqueEmployeeDateIndexName();

            if (fkName != null) {
                jdbcTemplate.execute("ALTER TABLE attendance DROP FOREIGN KEY `" + fkName + "`");
                log.info("Dropped attendance FK {}", fkName);
            }

            if (uniqueIndex != null) {
                jdbcTemplate.execute("ALTER TABLE attendance DROP INDEX `" + uniqueIndex + "`");
                log.info("Dropped attendance unique index {}", uniqueIndex);
            }

            ensureNonUniqueIndexesAndFk();
            log.info("Attendance schema updated for multiple check-in/out rows per day");
        } catch (Exception ex) {
            log.error("Failed to update attendance schema for multiple punches: {}", ex.getMessage(), ex);
        }
    }

    private void ensureNonUniqueIndexesAndFk() {
        if (!indexExists("idx_attendance_employee_id")) {
            jdbcTemplate.execute("ALTER TABLE attendance ADD INDEX idx_attendance_employee_id (employee_id)");
        }
        if (!indexExists("idx_attendance_employee_date")) {
            jdbcTemplate.execute(
                    "ALTER TABLE attendance ADD INDEX idx_attendance_employee_date (employee_id, attendance_date)"
            );
        }
        if (findEmployeeForeignKeyName() == null) {
            jdbcTemplate.execute(
                    "ALTER TABLE attendance ADD CONSTRAINT fk_attendance_employee "
                            + "FOREIGN KEY (employee_id) REFERENCES employee(id)"
            );
        }
    }

    private boolean hasUniqueEmployeeDateIndex() {
        return findUniqueEmployeeDateIndexName() != null;
    }

    private String findUniqueEmployeeDateIndexName() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT INDEX_NAME, GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS cols "
                        + "FROM information_schema.STATISTICS "
                        + "WHERE TABLE_SCHEMA = DATABASE() "
                        + "AND TABLE_NAME = 'attendance' "
                        + "AND NON_UNIQUE = 0 "
                        + "AND INDEX_NAME <> 'PRIMARY' "
                        + "GROUP BY INDEX_NAME"
        );
        for (Map<String, Object> row : rows) {
            String cols = String.valueOf(row.get("cols"));
            if (cols.contains("employee_id") && cols.contains("attendance_date")) {
                return String.valueOf(row.get("INDEX_NAME"));
            }
        }
        return null;
    }

    private String findEmployeeForeignKeyName() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE "
                        + "WHERE TABLE_SCHEMA = DATABASE() "
                        + "AND TABLE_NAME = 'attendance' "
                        + "AND COLUMN_NAME = 'employee_id' "
                        + "AND REFERENCED_TABLE_NAME = 'employee' "
                        + "LIMIT 1"
        );
        if (rows.isEmpty() || rows.get(0).get("CONSTRAINT_NAME") == null) {
            return null;
        }
        return String.valueOf(rows.get(0).get("CONSTRAINT_NAME"));
    }

    private boolean indexExists(String indexName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.STATISTICS "
                        + "WHERE TABLE_SCHEMA = DATABASE() "
                        + "AND TABLE_NAME = 'attendance' "
                        + "AND INDEX_NAME = ?",
                Integer.class,
                indexName
        );
        return count != null && count > 0;
    }
}
