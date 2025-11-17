package com.gamex.app;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {

    private DateUtils() {
        // Utility class
    }

    public static String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) {
            return "";
        }

        try {
            // Parse ISO 8601 format: "2025-11-16T10:07:23.000000Z"
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US);
            Date date = isoFormat.parse(isoDate);

            if (date != null) {
                // Format to readable date: "16 Nov 2025, 10:07"
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("id", "ID"));
                return displayFormat.format(date);
            }
        } catch (ParseException e) {
            // Try alternative format without microseconds
            try {
                SimpleDateFormat altFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                Date date = altFormat.parse(isoDate);

                if (date != null) {
                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("id", "ID"));
                    return displayFormat.format(date);
                }
            } catch (ParseException ex) {
                // Return original string if parsing fails
                return isoDate;
            }
        }

        return isoDate;
    }
}
