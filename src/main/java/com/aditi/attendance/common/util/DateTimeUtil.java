package com.aditi.attendance.common.util;

import java.time.Duration;
import java.time.LocalTime;

public final class DateTimeUtil {

    private DateTimeUtil() {
    }

    public static Duration calculateWorkingHours(
            LocalTime checkIn,
            LocalTime checkOut) {

        if (checkIn == null || checkOut == null) {
            return null;
        }

        return Duration.between(checkIn, checkOut);
    }

}