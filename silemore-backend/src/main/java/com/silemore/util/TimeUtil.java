package com.silemore.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public final class TimeUtil {
    public static final ZoneId APP_ZONE = ZoneId.of("Asia/Shanghai");

    private TimeUtil() {
    }

    public static LocalDate today() {
        return LocalDate.now(APP_ZONE);
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(APP_ZONE);
    }

    public static OffsetDateTime nowOffset() {
        return OffsetDateTime.now(APP_ZONE);
    }
}
