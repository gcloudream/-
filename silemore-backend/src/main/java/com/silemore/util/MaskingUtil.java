package com.silemore.util;

public final class MaskingUtil {
    private MaskingUtil() {
    }

    public static String maskEmail(String email) {
        if (email == null) {
            return null;
        }
        int at = email.indexOf('@');
        if (at <= 0) {
            return email;
        }
        String local = email.substring(0, at);
        String domain = email.substring(at);
        if (local.length() <= 2) {
            return local.substring(0, 1) + "**" + domain;
        }
        return local.substring(0, 2) + "**" + domain;
    }
}
