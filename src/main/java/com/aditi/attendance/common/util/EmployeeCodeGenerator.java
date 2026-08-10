package com.aditi.attendance.common.util;

import java.util.UUID;

public final class EmployeeCodeGenerator {

    private EmployeeCodeGenerator() {
    }

    public static String generate() {
        return Constants.EMPLOYEE_CODE_PREFIX + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}
