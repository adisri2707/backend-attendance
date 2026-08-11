package com.aditi.attendance.common.util;

public final class Constants {

    private Constants() {
    }

    public static final String EMPLOYEE_CODE_PREFIX = "EMP";

    public static final String STATUS_PRESENT = "PRESENT";
    public static final String STATUS_ABSENT = "ABSENT";
    public static final String STATUS_HALF_DAY = "HALF_DAY";
    public static final String STATUS_LATE = "LATE";

    /** Punch status ids used by check-in / check-out */
    public static final int STATUS_CHECK_IN = 1;
    public static final int STATUS_CHECK_OUT = 2;

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_HR = "HR";
    public static final String ROLE_EMPLOYEE = "EMPLOYEE";

    public static final String SHIFT_START = "10:00";
    public static final String SHIFT_END = "19:00";

}